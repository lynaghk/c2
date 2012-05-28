(ns c2.util
  (:use [reflex.macros :only [computed-observable]]))

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

(defmacro profile
  "Profile `body` and print `descr`.
   Returns result of body."
  [descr & body]
  `(let [start# (.getTime (js/Date.))
         ret# (do ~@body)]
    (print (str ~descr ": " (- (.getTime (js/Date.)) start#) " msecs"))
    ret#))

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



(defmacro bind!
  "Merges `hiccup-el` onto `el` (selector or live node).
   Recalculates `hiccup-el` and updates DOM whenever any of the atoms dereferenced within `hiccup-el` changes state.
   Returns computed observable of hiccup element."
  [el hiccup-el]
  `(let [co# (computed-observable ~hiccup-el)
         $el# (c2.dom/->dom ~el)]
     
     (singult.core/merge! $el# @co#)
     (add-watch co# :update-dom #(singult.core/merge! $el# @co#))
  
    co#))
