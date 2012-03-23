(ns c2.ticks
  (:use [c2.maths :only [sq]]))
;;Implementation of "An Extension of Wilkinson’s Algorithm for Positioning Tick Labels on Axes" by Justin Talbot, Sharon Lin, and Pat Hanrahan:
;;
;;    http://graphics.stanford.edu/vis/publications/2010/labeling-preprint.pdf
;;
;;see also: http://www.justintalbot.com/research/axis-labeling/


(def Q "Preference-ordered list of nice step sizes"
  [1 5 2 2.5 4 3])

(defn index-of "Index of x in coll"
  [x coll]
  (first (for [[idx y] (map-indexed vector coll)
               :when (= y x)] idx)))

(defn label-range-contains-zero? [l-min l-max l-step]
  (and (> l-max 0) (< l-min 0) (zero? (mod l-min l-step))))

(defn simplicity
  "Objective function modeling niceness of step sizes and whether a range includes zero."
  [q Q j label-range-contains-zero]
  (let [v (if label-range-contains-zero 1 0)]
    (if (<= (count Q) 1)
      (+ (- 1 j) v)
      (+ (- 1 (/ (index-of q Q) (dec (count Q))) j)
         v))))

(defn max-simplicity [q Q j] (simplicity q Q j true))

(defn coverage
  "Objective function based on distances between extreme data and extreme labels"
  [d-min d-max l-min l-max]
  (- 1 (* 0.5 (/ (+ (sq (- d-max l-max))
                    (sq (- d-min l-min)))
                 (sq (* 0.1 (- d-max d-min)))))))

(defn max-coverage
  "When the label range is centered on the data range"
  [d-min d-max span]
  (let [d-range (- d-max d-min)]
    (if (> span d-range)
      (- 1 (sq (/ (- span d-range)
                  (* 0.2 d-range))))
      1)))

(defn density
  "Objective function for a candidate density r and desired density rt (e.g. labels-per-cm)"
  [r rt]
  ;;Note the formula should be 2-, not 1- as in the paper.
  (- 2 (max (/ r rt) (/ rt r))))

(defn max-density [r rt]
  (if (>= r rt)
    (- 2 (/ r rt))
    1))

(defn- w
  "Balance the relative merits of different metrics"
  [[simplicity coverage density legibility]]
  (let [w [0.2 0.25 0.5 0.05]]
    (+ (* simplicity (w 1))
       (* coverage (w 2))
       (* density (w 3))
       (* legibility (w 4)))))



(defn search
  "Find best ticks for the data range (dmin, dmax) and target label density m"
  [dmin dmax m]
  )
