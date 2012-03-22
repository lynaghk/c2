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


(defn simplicity
  "Objective function modeling niceness of step sizes and whether a range includes zero."
  [q Q j l-min l-max l-step]
  (let [v ;;Does the range contain zero?
        (and (> l-max 0)
             (< l-min 0)
             (zero? (mod l-min l-step)))]
    (if (<= (count Q) 1)
      (+ (- 1 j) v)
      (+ (- 1 (/ (index-of q Q) (dec (count Q))) j)
         v))))

(defn coverage
  "Objective function based on distances between extreme data and extreme labels"
  [d-min d-max l-min l-max]
  (- 1 (* 0.5 (/ (+ (sq (- d-max l-max))
                    (sq (- d-min l-min)))
                 (sq (* 0.1 (- d-max d-min)))))))

(defn density
  "Objective function for a candidate density r and desired density rt (e.g. labels-per-cm)"
  [r rt]
  ;;Note the formula should be 2-, not 1- as in the paper.
  (- 2 (max (/ r rt) (/ rt r))))
