(ns c2.util)

(defmacro p
  "Print and return native JavaScript argument."
  [x]
  `(let [res# ~x]
     (.log js/console res#)
     res#))

(defmacro pp
  "Pretty print and return argument (uses `prn-str` internally)."
  [x]
  `(let [res# ~x]
     (.log js/console (prn-str res#))
     res#))

(defmacro timeout [delay & body]
  `(js/setTimeout (fn [] ~@body) ~delay))
(defmacro interval [delay & body]
  `(js/setInterval (fn [] ~@body) ~delay))

(defmacro c2-obj
  "Define record and corresponding constructor that accepts keyword arguments.
   The constructor function is defined to be the given name, with the record having an underscore prefix."
  [name fields-with-defaults & body]
  (let [recname (symbol (str "_" (clojure.core/name name)))]
    `(do
       (defrecord ~recname ~(into [] (map (comp symbol clojure.core/name)
                                          (keys fields-with-defaults)))
         ~@body)
       (defn ~name [& ~'kwargs]
         (~(symbol (str "map->" (clojure.core/name recname)))
          (merge ~fields-with-defaults (apply hash-map ~'kwargs)))))))


 

(defmacro combine-with
  "Element-by-element operations between sequences.
   Used by c2.maths for vector arithmetic.
   Modified from Incanter."
  [A B op fun]
  `(cond
    (and (number? ~A) (number? ~B)) (~op ~A ~B)
    (and (coll? ~A) (coll? ~B)) (map ~op ~A ~B)
    (and (number? ~A) (coll? ~B)) (map ~op (replicate (count ~B) ~A)  ~B)
    (and (coll? ~A) (number? ~B)) (map ~op ~A (replicate (count ~A) ~B))))
