(ns c2.dom
  (:use-macros [c2.util :only [p pp timeout bind!]]
               [clojure.core.match.js :only [match]])
  (:require [clojure.string :as string]
            [singult.core :as singult]
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


(defn append!
  "Make element last child of container.
   Returns live DOM node.
   > *container* CSS selector or live DOM node
   > *el* hiccup vector"
  [container el]
  (let [el (if (dom-element? el)
             el
             (singult/render el))]
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
             (singult/render el))]
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
              :hiccup (singult/render new)
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
