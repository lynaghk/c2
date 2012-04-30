^:clj (ns c2.geo.geom
        (:use [clojure.core.match :only [match]]
              [clojure.string :only [join]]
              [c2.maths :only [abs add sub div mul]])
        (:require c2.geom.polygon))

^:cljs (ns c2.geo.geom
         (:use-macros [clojure.core.match.js :only [match]])
         (:use [c2.maths :only [abs add sub div mul]])
         (:require [c2.geom.polygon :as c2.geom.polygon]))


(defn area [geo & {:keys [projection]
                   :or {projection identity}}]

  (defn polygon-area [poly-coordinates]
    (let [area (fn [coordinates]
                 (abs (c2.geom.polygon/area (map projection coordinates))))]

      ;;area of exterior boundary - interior holes
      (apply - (area (first poly-coordinates))
             (map area (rest poly-coordinates)))))

  (abs (match [geo]
              [{:type "FeatureCollection" :features xs}]
              (apply + (map area xs))

              [{:type "Feature" :geometry g}]
              (area g)

              [{:type "Polygon" :coordinates xs}]
              (polygon-area xs)

              [{:type "MultiPolygon" :coordinates xs}]
              (apply + (map polygon-area xs)))))


(defn centroid [geo & {:keys [projection]
                       :or {projection identity}}]

  (defn polygon-centroid
    "Compute polygon centroid by geometric decomposition.
     http://en.wikipedia.org/wiki/Centroid#By_geometric_decomposition"
    [poly-coordinates]
    (let [areas (map (fn [coordinates]
                       (abs (c2.geom.polygon/area (map projection coordinates))))
                     poly-coordinates)]

      ;;Return hashmap containing the area so weighted centroid can be calculated for MultiPolygons.
      {:centroid (div (apply sub (map (fn [coordinates area]
                                        (mul (c2.geom.polygon/centroid coordinates)
                                             area))
                                      poly-coordinates areas))
                      (apply - areas))
       :area (apply + areas)}))

  (match [geo]
         [{:type "Feature" :geometry g}]
         (centroid g)

         [{:type "Polygon" :coordinates xs}]
         (:centroid (polygon-centroid xs))

         [{:type "MultiPolygon" :coordinates xs}]
         (let [centroids (map polygon-centroid xs)]
           (div (apply add (map (fn [{:keys [centroid area]}]
                                  (mul centroid area))
                                centroids))
                (apply add (map :area centroids))))))
