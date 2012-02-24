(ns c2.geo.core
  (:use [clojure.core.match :only [match]]
        [clojure.string :only [join]]))

(defn geo-to-svg
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
         (join (map geo-to-svg xs))

         [{:type "Feature" :geometry g}] (geo-to-svg g)

         [{:type "Polygon" :coordinates xs}]
         (join (map coords->path xs))

         [{:type "MultiPolygon" :coordinates xs}]
         ;;It'd be nice to recurse to the actual branch that handles Polygon, instead of repeating...
         (join (map (fn [subpoly]
                      (join (map coords->path subpoly)))
                    xs))))
