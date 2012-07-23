(ns c2.dom
  (:refer-clojure :exclude [val])
  (:use-macros [c2.util :only [p pp timeout bind!]]
               [clojure.core.match.js :only [match]])
  (:require [clojure.string :as string]
            [singult.core :as singult]
            [goog.dom :as gdom]
            [goog.dom.forms :as gforms]
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

(declare select)

(defprotocol IDom
  (->dom [x] "Converts x to a live DOM node"))

(extend-protocol IDom
  string
  (->dom [selector] (select selector))

  js/Node
  (->dom [node] node)

  PersistentVector
  (->dom [v] (singult/render v)))

(defn select
  "Select a single DOM node via CSS selector, optionally scoped by second arg."
  ([selector] (.querySelector js/document selector))
  ([selector container] (.querySelector (->dom container) selector)))
(defn select-all
  "Like select, but returns a collection of nodes."
  ([selector] (.querySelectorAll js/document selector))
  ([selector container] (.querySelectorAll (->dom container) selector)))

(defn matches-selector?
  "Does live `node` match CSS `selector`?"
  [node selector]
  (.webkitMatchesSelector node selector))

(defn children
  "Return the children of a live DOM element."
  [node]
  (.-children (->dom node)))

(defn parent
  "Return parent of a live DOM node."
  [node]
  (.-parentNode (->dom node)))


(defn append!
  "Make element last child of container.
   Returns live child."
  [container el]
  (let [el (->dom el)]
    (gdom/appendChild (->dom container) el)
    el))

(defn prepend!
  "Make element first child of container.
   Returns live DOM child."
  [container el]
  (let [el (->dom el)]
    (gdom/insertChildAt (->dom container) el 0)
    el))

(defn remove!
  "Remove element from DOM and return it.
   > *el* CSS selector or live DOM node"
  [el]
  (gdom/removeNode (->dom el)))

(defn replace!
  "Replace live DOM node with a new one, returning the latter.
   > *old* CSS selector or live DOM node
   > *new* CSS selector, live DOM node, or hiccup vector"
  [old new]
  (let [new (->dom new)]
    (gdom/replaceNode new (->dom old))
    new))

(defn style
  "Get or set inline element style.

   `(style el)`                map of inline element styles

   `(style el :keyword)`       value of style :keyword

   `(style el {:keyword val})` sets inline style according to map, returns element

   `(style el :keyword val)`   sets single style, returns element"
  ([el] (throw (js/Error. "TODO: return map of element styles")))
  ([el x]
     (let [el (->dom el)]
       (match [x]
              [(k :guard keyword?)] (gstyle/getComputedStyle el (name k))
              [(m :guard map?)]
              (do
                (doseq [[k v] m] (style el k v))
                el))))
  ([el k v]
     (gstyle/setStyle (->dom el) (name k)
                      (match [v]
                             [s :guard string?] s
                             [n :guard number?]
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
  ([el] (let [attrs (.-attributes (->dom el))]
          (into {} (for [i (range (.-length attrs))]
                     [(keyword  (.-name (aget attrs i)))
                      (.-value (aget attrs i))]))))
  ([el x]
     (let [el (->dom el)]
       (match [x]
              [(k :guard keyword?)] (.getAttribute el (name k))
              [(m :guard map?)]
              (do (doseq [[k v] m] (attr el k v))
                  el))))
  ([el k v]
     (let [el (->dom el)]
       (if (nil? v)
         (.removeAttribute el (name k))
         (if (= :style k)
           (style el v)
           (.setAttribute el (name k) v)))
       el)))

(defn text
  "Get or set element text, returning element"
  ([el]
     (gdom/getTextContent (->dom el)))
  ([el v]
     (let [el (->dom el)]
       (gdom/setTextContent el v)
       el)))

(defn val
  "Get or set element value."
  ([el]
     (gforms/getValue (->dom el)))
  ([el v]
     (let [el (->dom el)]
       (gforms/setValue el v)
       el)))

(defn classed!
  "Add or remove `class` to element based on boolean `classed?`, returning element."
  [el class classed?]
  (gclasses/enable (->dom el) (name class) classed?))

;;TODO: make these kind of shortcuts macros for better performance.
(defn add-class! [el class] (classed! el class true))
(defn remove-class! [el class] (classed! el class false))

;;Call this fn with a fn that should be executed on the next browser animation frame.
(def request-animation-frame
  (or (.-requestAnimationFrame js/window)
      (.-webkitRequestAnimationFrame js/window)
      #(timeout 10 (%))))
