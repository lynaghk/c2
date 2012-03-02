(ns c2.core
  (:use [clojure.string :only [join]]))

(defn unify
  ([data mapping]
     (map mapping data))
  ([container data mapping]
      (into container (unify data mapping))))


(defn style
  "Convert map to CSS string"
  [m]
  (join (for [[k v] m]
          (str (name k) ":" v ";"))))
