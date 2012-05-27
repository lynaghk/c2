(ns c2.dom
  (:use-macros [c2.util :only [p pp timeout bind!]]
               [clojure.core.match.js :only [match]]
               [iterate :only [iter]])
  (:require [clojure.string :as string]
            [goog.dom :as gdom]
            [goog.dom.classes :as gclasses]
            [goog.style :as gstyle]))

;;Seq over native JavaScript node collections
(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))
(extend-type js/HTMLCollection
  ISeqable
  (-seq [array] (array-seq array 0)))

;;Required for DOM nodes to be used in sets
(extend-type js/Node
  IHash
  (-hash [x] x))

;; Regular expression that parses a CSS-style id and class from a tag name. From Weavejester's Hiccup.
(def re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

;;Namespace URIs
(def xmlns {:xhtml "http://www.w3.org/1999/xhtml"
            :svg "http://www.w3.org/2000/svg"})

;;Common SVG tags; DOM node creation fn checks this set to infer element namespace (so users don't have to write things like [:svg:rect]).
(def svg-tags #{:svg :g :rect :circle :clipPath :path :line :polygon :polyline :text :textPath})


(defn dom-element?
  "Live DOM elements have JavaScript nodeName defined."
  [x]
  (not (undefined? (.-nodeName x))))

(defn node-type
  "Check node type; used as a dispatch fn."
  [node]
  (cond
   (vector? node)      :hiccup   ;;Hiccup vector
   (map? node)         :chiccup  ;;Hiccup map representation
   (string? node)      :selector ;;CSS selector string
   (dom-element? node) :dom      ;;It's an actual DOM node
   ))

(defmulti select
  "Select a single DOM node via CSS selector, optionally scoped by second arg.
   If passed live DOM node, just return it."
  node-type)
(defmethod select :selector
  ([selector] (.querySelector js/document selector))
  ([selector container] (.querySelector (select container) selector)))
(defmethod select :dom [node] node)

(defmulti select-all
  "Like select, but returns a collection of nodes."
  node-type)
(defmethod select-all :selector
  ([selector] (.querySelectorAll js/document selector))
  ([selector container] (.querySelectorAll (select container) selector)))

(defn matches-selector?
  "Does live `node` match CSS `selector`?"
  [node selector]
  (.webkitMatchesSelector node selector))

(defn children
  "Return the children of a live DOM element."
  [node]
  (filter #(= 1 (.-nodeType %))
          (.-childNodes (select node))))

(defn parent
  "Return parent of a live DOM node."
  [node]
  (.-parentNode (select node)))



(declare build-dom-elem)
(declare canonicalize)


(defn append!
  "Make element last child of container.
   Returns live DOM node.
   > *container* CSS selector or live DOM node
   > *el* hiccup vector"
  [container el]
  (let [el (if (dom-element? el)
             el
             (build-dom-elem el))]
    (gdom/appendChild (select container) el)
    el))

(defn prepend!
  "Make element first child of container.
   Returns live DOM node.
   > *container* CSS selector or live DOM node
   > *el* hiccup vector"
  [container el]
  (let [el (if (dom-element? el)
             el
             (build-dom-elem el))]
    (gdom/insertChildAt (select container) el 0)
    el))

(defn remove!
  "Remove element from DOM and return it.
   > *el* CSS selector or live DOM node"
  [el]
  (gdom/removeNode (select el)))

(defn replace!
  "Replace live DOM node with a new one.
   > *old* CSS selector or live DOM node
   > *new* CSS selector, live DOM node, or hiccup vector"
  [old new]
  (let [new (condp = (node-type new)
              :dom new
              :hiccup (build-dom-elem new)
              :selector (select new))]
    (gdom/replaceNode new (select old))))

(defn style
  "Get or set inline element style.

   `(style el)`                map of inline element styles

   `(style el :keyword)`       value of style :keyword

   `(style el {:keyword val})` sets inline style according to map, returns element

   `(style el :keyword val)`   sets single style, returns element"
  ([el] (throw (js/Error. "TODO: return map of element styles")))
  ([el x] (match [x]
                 [(k :when keyword?)] (gstyle/getComputedStyle el (name k))
                 [(m :when map?)]
                 (do
                   (doseq [[k v] m] (style el k v))
                   el)))
  ([el k v] (gstyle/setStyle el (name k)
                             (match [v]
                                    [s :when string?] s
                                    [n :when number?]
                                    (if (#{:height :width :top :left :bottom :right} (keyword k))
                                      (str n "px")
                                      n)))
     el))

(defn attr
  "Get or set element attributes.

   `(attr el)`                map of element attributes

   `(attr el :keyword)`       value of attr :keyword

   `(attr el {:keyword val})` sets element attributes according to map, returns element

   `(attr el :keyword val)`   sets single attr, returns element"
  ([el] (let [attrs (.-attributes el)]
          (into {} (for [i (range (.-length attrs))]
                     [(keyword  (.-name (aget attrs i)))
                      (.-value (aget attrs i))]))))
  ([el x] (match [x]
                 [(k :when keyword?)] (.getAttribute el (name k))
                 [(m :when map?)]
                 (do (doseq [[k v] m] (attr el k v))
                     el)))
  ([el k v]
     (if (nil? v)
       (.removeAttribute el (name k))
       (if (= :style k)
         (style el v)
         (.setAttribute el (name k) v)))
     el))

(defn text
  "Get or set element text."
  ([el]
     (gdom/getTextContent (select el)))
  ([el v]
     (gdom/setTextContent (select el) v)))

(defn classed!
  "Add or remove `class` to `el` based on boolean `classed?`."
  [el class classed?]
  (gclasses/enable (select el) class classed?))

;;TODO: make these kind of shortcuts macros for better performance.
(defn add-class! [el class] (classed! el class true))
(defn remove-class! [el class] (classed! el class false))

(defn whitespace-node? [$n]
  (and (= 3 (.-nodeType $n))
       (re-matches #"^\s+$" (.-textContent $n))))

;;Call this fn with a fn that should be executed on the next browser animation frame.
(def request-animation-frame
  (or (.-requestAnimationFrame js/window)
      (.-webkitRequestAnimationFrame js/window)
      #(timeout 10 (%))))

(defn merge!
  "Recursively walk a live DOM node, unifying its attributes and children with those of hiccup vector `el`.
   Boolean kwarg `:defer-attr` to set attributes on next animation frame, defaults to `false`. "
  [$node el & {:keys [defer-attr]
               :or {defer-attr false}}]

  (let [el (canonicalize el)]
    (when (not= (.toLowerCase (.-nodeName $node))
                (.toLowerCase (name (:tag el))))
      (p $node)
      (pp el)
      (throw "Cannot merge el into node of a different type"))

    (if defer-attr
      (request-animation-frame #(attr $node (:attr el)) $node)
      (attr $node (:attr el)))

    ;;merge children (implicitly assumes that nodes by index match up)
    (doseq [[$c c] (map vector
                        (remove whitespace-node? (.-childNodes $node))
                        (:children el))]
      (match [[(.-nodeType $c) c]]
             [[1 (m :when map?)]]    (merge! $c m :defer-attr defer-attr)
             [[3 (s :when string?)]] (set! (.-textContent $c) s)
             :else (do (p $c) (pp c) (throw "Cannot merge."))))
    $node))

(defn canonicalize
  "Convert hiccup vectors into maps suitable for rendering.
   Hiccup vectors will be converted to maps of {:tag :attr :children}.
   Strings will be passed through and numbers coerced to strings.
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
                                                     (let [tag (keyword nsp)]
                                                       [(if (svg-tags tag)
                                                          (:svg xmlns)
                                                          (:xhtml xmlns))
                                                        tag])))
                                   tag-attrs        (into {}
                                                          (filter #(not (nil? (second %)))
                                                                  {:id (or id nil)
                                                                   :class (if class (string/replace class #"\." " "))}))
                                   map-attrs        (first content)]

                               (let [[attr raw-children] (if (map? map-attrs)
                                                           [(merge-with #(str %1 " " %2) tag-attrs map-attrs)
                                                            (next content)]
                                                           [tag-attrs content])
                                     ;;Explode children seqs in place
                                     children (mapcat #(if (and (not (vector? %)) (seq? %))
                                                         (map canonicalize %)
                                                         [(canonicalize %)])
                                                      raw-children)]
                                 {:nsp nsp :tag tag :attr attr :children children}))))

(defn create-elem [nsp tag]
  (.createElementNS js/document nsp (name tag)))

(defn build-dom-elem
  "Build live DOM element from hiccup vector, hiccup map, or string."
  [el]
  (match [el]
         [(s :when string?)] (gdom/createTextNode s)
         [(v :when vector?)] (recur (canonicalize v))
         [(m :when map?)] ;;Can't use {:keys [...]} destructuring in place of m in this clause. Why?
         (let [{:keys [nsp tag children] :as elm} m
               elem (create-elem nsp tag)]
           (attr elem (:attr elm))
           (doseq [c (map build-dom-elem children)]
             (when c
               (append! elem c)))
           elem)))
