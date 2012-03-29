(ns c2.dom
  (:use-macros [c2.util :only [p timeout]]
               [clojure.core.match.js :only [match]]
               [iterate :only [iter]])
  (:require [clojure.string :as string]
            [goog.dom :as gdom]
            [goog.style :as gstyle]))

;; From Weavejester's Hiccup.
(def ^{:doc "Regular expression that parses a CSS-style id and class from a tag name."}
  re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(def xmlns {:xhtml "http://www.w3.org/1999/xhtml"
            :svg "http://www.w3.org/2000/svg"})


(defn dom-element? [x]
  (not (undefined? (.-nodeName x))))

(defn node-type [node]
  (cond
   (vector? node)      :hiccup   ;;Hiccup vector
   (map? node)         :chiccup  ;;Hiccup map representation
   (string? node)      :selector ;;CSS selector string
   (dom-element? node) :dom      ;;It's an actual DOM node
   ))

(defmulti select node-type)
(defmethod select :selector
  ([selector] (.querySelector js/document selector))
  ([selector container] (.querySelector (select container) selector)))
(defmethod select :dom [node] node)

(defmulti select-all node-type)
(defmethod select-all :selector
  ([selector] (.querySelectorAll js/document selector))
  ([selector container] (.querySelectorAll (select container) selector)))
(defmethod select-all :dom [nodes] nodes)



(defn children [node]
  (filter #(= 1 (.-nodeType %))
          (.-childNodes (select node))))

(defn parent [node]
  (.-parentNode (select node)))



(defn append! [container el]
  (let [el (if (dom-element? el)
             el
             (build-dom-elem el))]
    (gdom/appendChild (select container) el)
    el))


(defn attr
  ([el] (let [attrs (.-attributes el)]
          (into {} (for [i (range (.-length attrs))]
                     [(keyword  (.-name (aget attrs i)))
                      (.-value (aget attrs i))]))))
  ([el x] (match [x]
                 [(k :when keyword?)] (.getAttribute el (name k))
                 [(m :when map?)] (doseq [[k v] m] (attr el k v))))
  ([el k v]
     (if (= :style k)
       (style el v)
       (.setAttribute el (name k) v))))

(defn style
  ([el] (throw (js/Error. "TODO: return map of element styles")))
  ([el x] (match [x]
                 [(k :when keyword?)] (gstyle/getComputedStyle el (name k))
                 [(m :when map?)] (doseq [[k v] m] (style el k v))))
  ([el k v] (gstyle/setStyle el (name k) v)))

(defn text [el v]
  (gdom/setTextContent el v))

(def request-animation-frame
  (or (.-requestAnimationFrame js/window)
      (.-webkitRequestAnimationFrame js/window)
      #(timeout 10 %)))

(defn merge-dom!
  "Walks an existing dom-node and makes sure that it has the same attributes and children as the given el."
  [dom-node el & {:keys [defer-attr]
                  :or {defer-attr false}}]
  (let [el (cannonicalize el)]
    (when (not= (.toLowerCase (.-nodeName dom-node))
                (.toLowerCase (name (:tag el))))
      (throw "Cannot merge el into node of a different type"))
    
    (if defer-attr
      (request-animation-frame #(attr dom-node (:attr el)) dom-node)
      (attr dom-node (:attr el)))

    (when-let [txt (first (filter string? (:children el)))]
      (text dom-node txt))
    (iter {for [dom-child el-child] in (map vector (children dom-node)
                                            (remove string? (:children el)))}
          (merge-dom! dom-child el-child :defer-attr defer-attr))))

(defn cannonicalize
  "Parse hiccup-like vec into map of {:tag :attr :children}, or return string as itself.
   Based on Pinot's html/normalize-element."
  [x]
  (match [x]
         [(str :when string?)] str
         [(n   :when number?)] (str n)
         [(m   :when map?)] m ;;todo, actually check to make sure map has nsp, tag, attr, and children keys
         ;;todo, make explicit match here for attr map and clean up crazy Pinot logic below
         [[tag & content]]   (let [[_ tag id class] (re-matches re-tag (name tag))
                                   [nsp tag]     (let [[nsp t] (string/split tag #":")
                                                       ns-xmlns (xmlns (keyword nsp))]
                                                   (if t
                                                     [(or ns-xmlns nsp) (keyword t)]
                                                     [(:xhtml xmlns) (keyword nsp)]))
                                   tag-attrs        (into {}
                                                          (filter #(not (nil? (second %)))
                                                                  {:id (or id nil)
                                                                   :class (if class (string/replace class #"\." " "))}))
                                   map-attrs        (first content)]

                               (if (map? map-attrs)
                                 {:nsp nsp :tag tag :attr (merge tag-attrs map-attrs) :children  (map cannonicalize (next content))}
                                 {:nsp nsp :tag tag :attr tag-attrs :children  (map cannonicalize content)}))))

(defn create-elem [nsp tag]
  (.createElementNS js/document nsp (name tag)))

(defn build-dom-elem [el]
  (match [el]
         [(s :when string?)] (gdom/createTextNode s)
         [(v :when vector?)] (recur (cannonicalize v))
         [(m :when map?)] ;;Can't use {:keys [...]} destructuring in place of m in this clause. Why?
         (let [{:keys [nsp tag children] :as elm} m
               elem (create-elem nsp tag)]
           (attr elem (:attr elm))
           (doseq [c (map build-dom-elem children)]
             (when c
               (append! elem c)))
           elem)))
