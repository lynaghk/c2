(ns c2.t-ticks
  (:use midje.sweet
        [c2.ticks :only [search]]))

(fact "Tick search"
  (search [0 100] :length 1000)
  => (contains {:ticks [0 10.0 20.0 30.0 40.0 50.0 60.0 70.0 80.0 90.0 100.0]
                :extent [0 100]
                :min 0, :max 100.0, :step 10.0}))

(fact "Clamped tick search"
  (search [5.5 20] :clamp? false)
  => (contains {:ticks [5.0 10.0 15.0 20.0]
                :extent [5.0 20]
                :min 5.0, :max 20.0, :step 5.0})
  
  (search [5.5 20] :clamp? true)
  => (contains {:ticks [5.5 10.5 15.5]
                :extent [5.5 20]
                :min 5.5, :max 20.0, :step 5.0}))
