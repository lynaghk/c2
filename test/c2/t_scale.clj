(ns c2.t-scale
  (:use midje.sweet
        [c2.scale :only [linear log]]))

(let [s (linear)]
  (facts "Scale defaults "
         (:domain s) => [0 1]
         (:range s) => [0 1])
  
  (tabular
   (facts "Linear scale"
          (s ?x) => (roughly ?y))
   ?x    ?y
   0.5   0.5
   -1    -1
   -10   -10
   10    10)
  
  (facts "Derived linear scale"
         ((assoc s :range [0 2]) 0.5) => (roughly 1))

  )


