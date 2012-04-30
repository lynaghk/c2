^:clj (ns c2.geo.core
        (:use [clojure.core.match :only [match]]
              [clojure.string :only [join]]))

^:cljs (ns c2.geo.core
         (:refer-clojure :exclude [map])
         (:use-macros [clojure.core.match.js :only [match]]))


^:cljs (do ;;use JS native map and join fns---this is about 3 times faster than using CLJS.

         ;;taken from ClojureScript master; remove once a new release is cut.
         (defn into-array
           ([aseq]
              (into-array nil aseq))
           ([type aseq]
              (reduce (fn [a x] (.push a x) a) (array) aseq)))
         
         (defn ->arr [c]
           (if (= js/Array (type c))
             c
             (into-array c)))

         (defn join
           ([c] (join "" c))
           ([sep c] (.join (->arr c) sep)))

         (defn map [f c] (.map (->arr c) f)))


(defn geo->svg
  "Convert geoJSON to svg path data. Takes optional projection, defaulting to identity"
  [geo & {:keys [projection]
          :or {projection identity}}]

  (let [project (fn [coordinate]
                  (join "," (projection coordinate)))
        coords->path (fn [coordinates]
                       (str "M"
                            (join "L" (map project coordinates))
                            "Z"))]

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
                      xs)))))
