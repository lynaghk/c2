(ns c2.util
  (:require [singult.core :as singult]))

(defn ->coll
  "Convert something into a collection, if it's not already."
  [x]
  (if (coll? x) x [x]))
