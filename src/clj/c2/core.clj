(ns c2.core)

(defn unify [container data mapping]
  (into container (map mapping data)))
