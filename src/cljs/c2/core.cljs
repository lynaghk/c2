(ns c2.core
  (:use-macros [c2.util :only [p timeout]])
  (:use [cljs.reader :only [read-string]]
        [c2.dom :only [select select-all node-type append! remove! children build-dom-elem merge-dom! attr cannonicalize]])
  (:require [goog.dom :as gdom]
            [clojure.set :as set]
            [clojure.string :as string]))

;;Seq over native JavaScript node collections
(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))
(extend-type js/HTMLCollection
  ISeqable
  (-seq [array] (array-seq array 0)))

;;This is required so that DOM nodes can be used in sets
(extend-type js/Node
  IHash
  (-hash [x] x))

(def node-data-key "c2")
(defmulti attach-data (fn [node d] (node-type node)))
(defmethod attach-data :chiccup [node d]
  (assoc node :data
         (binding [*print-dup* true] (pr-str d))))

(defmethod attach-data :dom [node d]
  (set! (.-__c2data__ node) d)
  node)

(defmulti read-data (fn [node] (node-type node)))
(defmethod read-data :chiccup [node]
  (read-string (:data node)))
(defmethod read-data :dom [node]
  (let [d (.-__c2data__ node)]
    (if (undefined? d) nil d)))


;; (defmulti unify!
;;   (fn [_ data & args]
;;     (cond (instance? cljs.core.Atom data) :atom
;;           (seq? data)                        :seq
;;           :else                              (throw (js/Error. "Unify! requires data to be seqable or an atom containing seqable")))))
;; (defmethod unify! :atom
;;   [_ atom-data & args]
;;   (apply unify! _ @atom-data args))

;;(defmethod unify! :seq)


;;Used to generate unique IDs for auto-unify atom watchers
(def ^:private auto-unify-id (atom 0))

(defn unify!
  "Calls (mapping datum idx) for each datum and appends resulting elements to container.
Automatically updates elements mapped to data according to key-fn (defaults to index) and removes elements that don't match.
Scoped to selector, if given, otherwise applies to all container's children.
Optional enter, update, and exit functions called before DOM is changed; return false to prevent default behavior."
  [container data mapping & {:keys [selector key-fn pre-fn post-fn update exit enter
                                    defer-attr force-update]
                             :or {key-fn (fn [d idx] idx)
                                  enter  (fn [d idx new-node]
                                           #_(p "no-op enter called")
                                           true)
                                  update (fn [d idx old-node new-node]
                                           #_(p "no-op update called")
                                           true)
                                  exit   (fn [d idx old-node]
                                           #_(p "default remove called")
                                           true)
                                  defer-attr false}}]

  (let [container (select container)
        ;;This logic should be abstracted out via a (unify!) multimethod, once (apply multimethod) is fixed in ClojureScript
        data (if (instance? cljs.core.Atom data)
               ;;Then add a watcher to auto-unify when the atom changes, and deference data for this run
               (do (add-watch data (keyword (str "auto-unify" (swap! auto-unify-id inc)))
                              (fn [key data-atom old new]
                                (p "atom updated; automatically calling unify!")
                                (unify! container @data-atom mapping
                                        :selector selector
                                        :key-fn key-fn
                                        :enter  enter
                                        :update update
                                        :exit exit
                                        :pre-fn pre-fn
                                        :post-fn post-fn
                                        :defer-attr defer-attr
                                        :force-update force-update)))
                   @data)
               data)

        data (if pre-fn
               (pre-fn data)
               data)


        existing-nodes-by-key (into {} (map-indexed (fn [i node]
                                                      (let [datum (read-data node)]
                                                        [(key-fn datum i)  {:node node
                                                                            :idx i
                                                                            :datum datum}]))
                                                    (if selector
                                                      (select-all selector container)
                                                      (children container))))]

    ;;Remove any stale nodes
    (doseq [k (set/difference (set (keys existing-nodes-by-key))
                              (set (map key-fn data (range))))]
      (let [{:keys [node idx datum]} (existing-nodes-by-key k)]
        (if (exit datum idx node)
          (remove! node))))


    ;;For each datum, update existing nodes and add new ones
    (doseq [[idx d] (map-indexed vector data)]
      (let [new-node (cannonicalize (mapping d idx))]
        ;;If there's an existing node
        (if-let [old (existing-nodes-by-key (key-fn d idx))]
          (do
            ;;append it (effectively moving it to the correct index in the container)
            (append! container (:node old))
            ;;If its data is not equal to the new data, update it
            (if (or (not= d (:datum old)) force-update)
              (if (update d idx (:node old) new-node)
                (attach-data (merge-dom! (:node old) new-node
                                         :defer-attr defer-attr)
                             d))))

          ;;instantiate new node on the DOM so it can be manipulated in the user-specified `enter` fn.
          (let [new-dom-node (append! "body" new-node)]
            (attach-data new-dom-node d)
            (if (enter d idx new-dom-node)
              (append! container new-dom-node) ;;move new node to container
              (remove! new-dom-node))))))

    ;;Run post-fn, if it was given
    (if post-fn
      ;;Give the browser 10 ms to get its shit together, if the post-fn involves advanced layout.
      ;;Without this delay, CSS3 animations sometimes don't happen.
      (timeout 10 #(post-fn data)))))
