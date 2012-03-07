(ns choropleth
  (:use [c2.core :only [unify style]]
        [c2.maths :only [extent floor]]
        [c2.geo.core :only [geo->svg]]
        [c2.geo.projection :only [albers-usa]]
        [vomnibus.geo.us :only [states]])
  (:require [c2.scale :as scale]
            [vomnibus.color-brewer :as color-brewer]))



(let [data (into {} (map vector (keys states) (repeatedly rand)))
      
      color-scheme color-brewer/Blues-7
      color-scale (let [s (scale/linear :domain (extent (vals data))
                                        :range [0 (dec (count color-scheme))])]
                    ;;todo: build interpolators so scales handle non-numeric ranges
                    (fn [d] (nth color-scheme (floor (s d)))))

      proj (albers-usa)]

  [:svg {:xmlns "http://www.w3.org/2000/svg"
           :preserveAspectRatio "xMinYMin meet"
           :width 700 :height 400
           :viewBox "0 0 950 500"}

     [:g.states
      (unify data
             (fn [[state-name val]]
               [:path.state {:d (geo->svg (get states state-name)
                                          :projection proj)
                             :stroke "black"
                             :fill (color-scale val)}]))]])
