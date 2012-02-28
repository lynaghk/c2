(ns c2.geom.polygon)

;;Vector arithmetic . TODO: refer-clojure :exclude and use multimethods for nicer lookin' code.

(defn add
  ([v1] v1)
  ([v1 v2] (map + v1 v2))
  ([v1 v2 & rest] (reduce add (add v1 v2) rest)))

(defn s-divide
  ([v1] v1)
  ([v1 s] (map / v1 (repeat s))))


(defn close-coordinates
  "Make sure a list of polygon coordinates is closed by adding the first coordinate to the end"
  [coordinates]
  (concat coordinates
          [(first coordinates)]))

(defn area
  "Calculate area from list of counterclockwise coordinates.
   http://en.wikipedia.org/wiki/Polygon#Area_and_centroid"
  [coordinates]
  (* 0.5 (apply + (map (fn [[[x0 y0] [x1 y1]]]
                         (- (* y0 x1)
                            (* x0 y1)))
                       (partition 2 1 (close-coordinates coordinates))))))

(defn centroid
  "Calculate centroid from list of counterclockwise coordinates.
   http://en.wikipedia.org/wiki/Polygon#Area_and_centroid"
  [coordinates]
  (s-divide (apply add (map (fn [[[x0 y0] [x1 y1]]]
                              (let [cross (- (* y0 x1) (* x0 y1))]
                                [(* cross (+ x0 x1)), (* cross (+ y0 y1))]))
                            (partition 2 1 (close-coordinates coordinates))))
            (* 6 (area coordinates))))
