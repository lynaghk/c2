(ns c2.util
  (:require [singult.core :as singult]))

(def clj->js singult/clj->js)

(defn ->coll
  "Convert something into a collection, if it's not already."
  [x]
  (if (coll? x) x [x]))
