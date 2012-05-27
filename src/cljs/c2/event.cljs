(ns c2.event
  (:use [cljs.reader :only [read-string]]
        [c2.core :only [node-data]])
  (:require [c2.dom :as dom]
            [goog.events :as gevents]))

(defn on-load
  "Execute fn when browser load event fires."
  [f]
  (.listen goog.events js/window goog.events.EventType.LOAD f))

(defn on-raw
  "Attach `event-type` handler `f` to `node`, a CSS selector or live DOM node.
   Event type is something like `:click` or `:mousemove`."
  [node event-type f]
  (gevents/listen (dom/->dom node) (name event-type) f))

(defn on
  "Attach delegate `event-type` event handler `f` to `node` whose children were created via `c2.core/unify!`, scoped by optional `selector`.
   Handler is called with datum, $node, and the event object.

   Example usage:

       (unify! \"#scatterplot\" data-set (fn [[x y]] [:circle {:cx x :cy y}]))
       (on \"#scatterplot\" :click (fn [d] (p (str \"circle clicked:\" (prn-str d)))))

   This method should be preferred over attaching event handlers to individual nodes created by a `unify!` call because it creates a single event handler on the parent instead of a handler on each child."
  ([node event-type f] (on node "*" event-type f))
  ([node selector event-type f]
     (gevents/listen (dom/->dom node)
                     (name event-type)
                     (fn [event]
                       ;;Check to see if the target is what we want to listen to.
                       ;;This could be, say, a data-less button that is a child of a node with c2 data.
                       (if (dom/matches-selector? (.-target event) selector)
                         ;;Loop through the parent nodes of the event origin node, event.target, until we reach one with c2 data attached.
                         (loop [$node (.-target event)]
                           (if-let [d (node-data $node)]
                             ;;Then, call the handler on this node
                             (f d $node event)
                             (if-let [parent (dom/parent $node)]
                               (recur parent)))))))))




