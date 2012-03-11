(ns c2.core
  (:use [clojure.string :only [join]]))

(defn unify
  ([data mapping]
     (map mapping data))
  ([container data mapping]
      (into container (unify data mapping))))


(defn style
  "Convert map to CSS string. Optional :numeric-suffix added to numbers (defaults to 'px')."
  [m & {:keys [numeric-suffix]
        :or {numeric-suffix "px"}}]
  (join (for [[k v] m]
          (str (name k) ":"
               (if (number? v)
                 (str v numeric-suffix)
                 v)
               ";"))))
