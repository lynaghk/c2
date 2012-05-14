;;Core functions that map data to DOM elements.
(ns c2.core
  (:use-macros [c2.util :only [p pp]])
  (:use [cljs.reader :only [read-string]]
        [c2.dom :only [select select-all node-type append! remove! children build-dom-elem merge! request-animation-frame]])
  (:require [goog.dom :as gdom]
            [clojure.set :as set]
            [clojure.string :as string]))

;;Attach data to live dom elements
(def node-data-key "c2")
(defn attach-data [$node d]
  (set! (.-__c2data__ $node) d)
  $node)
(defn read-data [$node]
  (let [d (.-__c2data__ $node)]
    (if (undefined? d) nil d)))



;; Given:
;;
;; > *container* CSS selector or live DOM node
;;
;; > *data* seqable or IWatchable that derefs to seqable
;;
;; > *mapping* fn of datum which returns Hiccup vector
;;
;; calls (mapping datum) for each datum and appends resulting elements to container.
;; Automatically updates elements mapped to data according to `key-fn` (defaults to index) and removes elements that don't match.
;;
;; Kwargs fns (args prefixed with $ are live DOM nodes):
;;
;; > *:enter*  `(fn [d idx $node])`
;;
;; > *:update* `(fn [d idx $old new])`
;;
;; > *:exit*   `(fn [d idx $node])`
;;
;; called before DOM changed; return false to prevent default behavior.
;;
;; Other kwargs:
;;
;; > *:selector* CSS selector to scope elements; `unify!` defaults to all container children
;;
;; > *:key-fn* fn of datum and index that associates (potentially live) nodes with new data, defaults to index
;;
;; > *:defer-attr* update attributes on next animation frame instead of immediately, defaults to `false`
;;
;; > *:force-update* update node even if data is identical; useful if mapping fn references mutable state, defaults to `false`
;;
;; If data implements IWatchable, DOM will update when data changes.
(defmulti unify!
  (fn [container data mapping & kwargs]
    (cond (satisfies? cljs.core.IWatchable data) :watchable
          (satisfies? cljs.core.ISeqable data)   :seq
          :else (do (pp data)
                    (throw (js/Error. "Unify! requires data to be seqable or IWatchable that derefs to seqable."))))))

;;Adds watcher to atom and runs unify! whenever the atom is updated.
(defmethod unify! :watchable
  [container !data & args]
  (let [redraw! #(apply unify! container % args)]
    (add-watch !data (keyword (gensym "auto-unify!-"))
               (fn [_ _ old new] (when (not= old new)
                                   (redraw! new))))
    ;;initial draw
    (redraw! @!data)))

(defmethod unify! :seq
  [container data mapping & {:keys [selector key-fn pre-fn post-fn update exit enter
                                    defer-attr force-update]
                             :or {key-fn (fn [d idx] idx)
                                  defer-attr false}}]

  (let [container (select container)
        data (if pre-fn (pre-fn data) data)
        existing-nodes-by-key (into {} (map-indexed (fn [idx node]
                                                      (let [datum (read-data node)]
                                                        [(key-fn datum idx) {:node node
                                                                             :idx idx
                                                                             :datum datum}]))
                                                    (if selector
                                                      (select-all selector container)
                                                      (children container))))]

    ;;Remove any stale nodes
    (doseq [k (set/difference (set (keys existing-nodes-by-key))
                              (set (map key-fn data (range))))]
      (let [{:keys [node idx datum]} (existing-nodes-by-key k)]
        (when (or (nil? exit)
                  (exit datum idx node))
          (remove! node))))


    ;;For each datum, update existing nodes and add new ones
    (doseq [[idx d] (map-indexed vector data)]
      (when-let [new-node (mapping d idx)]
        ;;If there's an existing node
        (if-let [old (existing-nodes-by-key (key-fn d idx))]
          (do
            ;;append it (effectively moving it to the correct index in the container)
            (append! container (:node old))
            (when (and (or (not= d (:datum old))
                           force-update)
                       (or (nil? update)
                           (update d idx (:node old) new-node)))
              (attach-data (merge! (:node old) new-node
                                   :defer-attr defer-attr)
                           d)))

          (let [$new-node (-> new-node
                              (build-dom-elem)
                              (attach-data d))]
            (when (or (nil? enter)
                      (enter d idx $new-node))
              (append! container $new-node))))))

    ;;Run post-fn, if it was given
    (when post-fn (request-animation-frame #(post-fn data)))))
