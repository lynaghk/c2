(ns c2.t-svg
  (:use midje.sweet
        [c2.svg :only [line]]))

(tabular
  (facts "Lines"
        (line ?coords) => ?expected)
  ?coords               ?expected
  [[0,0] [1,1] [5,5]]   [:path {:d "M0,0L1,1L5,5"}])

