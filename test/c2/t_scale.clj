(ns c2.t-scale
  (:use midje.sweet
        [c2.scale :only [invert linear log power]]))

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
  
  (let [derived (assoc s :range [0 2])]
    (facts "Derived linear scale"
           (derived 0.5) => (roughly 1))

    (facts "Inverted linear scale"
           ((invert derived) 2) => (roughly 1))))


(let [s (log)]
  (tabular
   (facts "log scale"
          (s ?x) => (roughly ?y))
   ?x    ?y
   1      0
   0.1   -1
   100    2))

(let [s (power :domain [0 10]
               :range [0 10])]
  (tabular
   (facts "power scale"
          (s ?x) => (roughly ?y))
   ?x    ?y
   0      0
   5      0.06692850924284854
   10     10))
