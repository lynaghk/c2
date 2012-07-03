(ns c2.t-scale
  (:use midje.sweet
        [c2.ticks :only [search]]))

(fact "Tick search"
  (search [0 100] :length 1000)
  => (contains {:ticks [0 10.0 20.0 30.0 40.0 50.0 60.0 70.0 80.0 90.0 100.0]
                :extent [0 100]
                :min 0, :max 100.0, :step 10.0}))
