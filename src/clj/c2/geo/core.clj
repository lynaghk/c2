(ns c2.geo.core
  (:use [clojure.core.match :only [match]]
        [clojure.string :only [join]]
        [c2.maths :only [abs add sub div mul]])
  (:require c2.geom.polygon))

(defn geo->svg
  "Convert geoJSON to svg path data. Takes optional projection, defaulting to identity"
  [geo & {:keys [projection]
          :or {projection identity}}]

  (defn project [coordinate]
    (join "," (projection coordinate)))
  (defn coords->path [coordinates]
    (str "M"
         (join "L" (map project coordinates))
         "Z"))
  ;;See http://geojson.org/geojson-spec.html
  ;;This SVG rendering doesn't implement the full spec.
  (match [geo]
         [{:type "FeatureCollection" :features xs}]
         (join (map #(geo->svg % :projection projection) xs))

         [{:type "Feature" :geometry g}] (geo->svg g :projection projection)

         [{:type "Polygon" :coordinates xs}]
         (join (map coords->path xs))

         [{:type "MultiPolygon" :coordinates xs}]
         ;;It'd be nice to recurse to the actual branch that handles Polygon, instead of repeating...
         (join (map (fn [subpoly]
                      (join (map coords->path subpoly)))
                    xs))))



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
