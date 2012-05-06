(ns c2.geom.polygon
  (:use [c2.maths :only [add div]]))

(defn close-coordinates
  "Closes a collection of coordinates by adding the first coordinate to the end"
  [coordinates]
  (concat coordinates
          [(first coordinates)]))

(defn area
  "Calculate area from list of counterclockwise coordinates,
   ref [Wikipedia](http://en.wikipedia.org/wiki/Polygon#Area_and_centroid)"
  [coordinates]
  (* 0.5 (apply + (map (fn [[[x0 y0] [x1 y1]]]
                         (- (* y0 x1)
                            (* x0 y1)))
                       (partition 2 1 (close-coordinates coordinates))))))

(defn centroid
  "Calculate centroid from list of counterclockwise coordinates,
   ref [Wikipedia](http://en.wikipedia.org/wiki/Polygon#Area_and_centroid)"
  [coordinates]
  (div (apply add (map (fn [[[x0 y0] [x1 y1]]]
                              (let [cross (- (* y0 x1) (* x0 y1))]
                                [(* cross (+ x0 x1)), (* cross (+ y0 y1))]))
                            (partition 2 1 (close-coordinates coordinates))))
            (* 6 (area coordinates))))
