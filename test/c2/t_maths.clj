(ns c2.t-scale
  (:use midje.sweet
        c2.maths))

(fact "Quantiles are calculated correctly"
  (map - (quantile (range 1 101))
       [1.00 25.75 50.50 75.25 100.00]) => (has every? (roughly 0)))

(fact "Radians and degrees seem to be converted properly"
  (rad 90) => (/ Tau 4)
  (rad 180) => (-> Tau (* 3) (/ 4))
  (deg (/ Tau 5)) => 72
  (deg (-> Tau (* 7) (/ 8))) => 315)
