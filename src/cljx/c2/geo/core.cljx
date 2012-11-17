^:clj (ns c2.geo.core
        (:use [c2.maths :only [rad sq sqrt sin asin cos radians-per-degree]]
              [clojure.core.match :only [match]]
              [clojure.string :only [join]]))

^:cljs (ns c2.geo.core
         (:refer-clojure :exclude [map])
         (:use-macros [clojure.core.match.js :only [match]])
         (:use [c2.maths :only [rad sq sqrt sin asin cos radians-per-degree]]))


;;Use JS native map and join fns; this is about 3 times faster than using CLJS seqs and str.
^:cljs (do
         
         (defn ->arr [c]
           (if (= js/Array (type c))
             c
             (into-array c)))

         (defn join
           ([c] (join "" c))
           ([sep c] (.join (->arr c) sep)))

         (defn map [f c] (.map (->arr c) f)))


(defn geo->svg
  "Convert geoJSON to SVG path data.

   Kwargs:
   > *:projection* fn applied to each coordinate, defaults to identity"
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

(defn ->latlon
  "Convert coordinates (potentially map of `{:lat :lon}`) to 2-vector."
  [coordinates]
  (cond
   (and (vector? coordinates)
        (= 2 (count coordinates)))
   coordinates
        
   (map? coordinates)
   (let [{lat :lat lon :lon} coordinates]
     [lat lon])))

(def radius-of-earth
  "Radius of OUR AWESOME PLANET, in kilometers"
  6378.1)

(defn haversine
  "Calculate the great-circle distance between two lat/lon coordinates on a sphere with radius `r` (defaults to Earth radius)."
  ([c1 c2] (haversine c1 c2 radius-of-earth))
  ([c1 c2 r]
     (let [[lat1 lon1] (->latlon c1)
           [lat2 lon2] (->latlon c2)
           square-half-chord (+ (sq (sin (/ (rad (- lat2 lat1)) 2)))
                                (* (cos (rad lat1))
                                   (cos (rad lat2))
                                   (sq (sin (/ (rad (- lon2 lon1)) 2)))))
           angular-distance (* (asin (sqrt square-half-chord)) 2)]
       (* angular-distance r))))
