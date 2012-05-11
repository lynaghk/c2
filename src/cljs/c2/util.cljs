(ns c2.util)

;;Taken from Mark McGranaghan
;;http://mmcgrana.github.com/2011/09/clojurescript-nodejs.html
(defn clj->js
  "Recursively transforms ClojureScript maps into Javascript objects,
   other ClojureScript colls into JavaScript arrays, and ClojureScript
   keywords into JavaScript strings."
  [x]
  (cond
   (string? x)  x
   (keyword? x) (name x)
   (map? x)     (reduce (fn [o [k v]]
                          (let [key (clj->js k)]
                            (when-not (string? key)
                              (throw "Cannot convert; JavaScript map keys must be strings"))
                            (aset o key (clj->js v))
                            o))
                        (js-obj) x)
   (coll? x)    (apply array (map clj->js x))
   :else x))



(defn ->coll
  "Convert something into a collection, if it's not already."
  [x]
  (if (coll? x) x [x]))
