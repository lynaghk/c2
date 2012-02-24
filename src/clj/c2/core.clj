(ns c2.core)

(defn unify
  ([data mapping]
     (map mapping data))
  ([container data mapping]
      (into container (unify data mapping))))
