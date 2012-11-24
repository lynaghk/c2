(ns c2.layout.t-histogram
  (:use midje.sweet
        [c2.layout.histogram :only [histogram]]))

(let [data [{:name "sally" :age 20}
            {:name "al" :age 55}
            {:name "ali" :age 56}
            {:name "amanda" :age 12}
            {:name "andy" :age 26}
            {:name "brock" :age 30}]]

  (tabular
    (facts "Bins"
           (let [h (histogram data :value :age :bins ?bin-count)]
             (map (comp :y meta) h) => ?expected
             (map count h) => ?expected))
    ?bin-count ?expected
    1          '(6)
    2          '(4 2)
    3          '(3 1 2)
    4          '(2 2 0 2)
    5          '(2 1 1 0 2)
    6          '(1 2 1 0 0 2))

  (tabular
    (facts "Range"
           (let [h (histogram data :value :age :range ?range)]
             (reduce + (map (comp :y meta) h)) => ?expected
             (reduce + (map count h)) => ?expected))
    ?range     ?expected
    [10 60]    6
    [13 60]    5
    [14 56]    5
    [20 40]    3
    [55 56]    2
    [-10 0]    0))
