(ns c2.event
  (:use [cljs.reader :only [read-string]]
        [c2.core :only [read-data dont-carity node-type]])
  (:require [c2.dom :as dom]
            [goog.events :as gevents]))

(defn matches-selector? [node selector]
  (.webkitMatchesSelector node selector))

(defn on-load [f]
  (.listen goog.events js/window goog.events.EventType.LOAD f))

(defn on-raw [node event-type f]
  (gevents/listen (dom/select node) (name event-type) f))

(defn on
  ([node event-type f] (on node "*" event-type f))
  ([node selector event-type f]
     (gevents/listen (dom/select node)
                     (name event-type)
                     (fn [event]
                       ;;Check to see if the target is what we want to listen to.
                       ;;This could be, say, a data-less button that is a child of a node with c2 data.
                       (if (matches-selector? (.-target event) selector)
                         ;;Loop through the parent nodes of the event origin node, event.target, until we reach one with c2 data attached.
                         (loop [node (.-target event)]
                           (if-let [d (read-data node)]
                             ;;Then, call the handler on this node
                             (dont-carity f d node event)
                             (if-let [parent (dom/parent node)]
                               (recur parent)))))))))




