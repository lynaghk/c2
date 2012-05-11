(ns choropleth
  (:use [c2.core :only [unify]]
        [c2.maths :only [extent floor]]
        [c2.geo.core :only [geo->svg]]
        [c2.geo.projection :only [albers-usa]]
        [vomnibus.geo.us.states :only [states]])
  (:require [c2.scale :as scale]
            [vomnibus.color-brewer :as color-brewer]))



(let [data (map (fn [[state geo]]
                  {:state state
                   :geo geo
                   :value (rand)})
                states)
      
      color-scheme color-brewer/Greens-9
      color-scale (let [s (scale/linear :domain (extent (map :value data))
                                        :range [0 (dec (count color-scheme))])]
                    ;;todo: build interpolators so scales handle non-numeric ranges
                    (fn [d] (nth color-scheme (floor (s d)))))

      proj (albers-usa)]

  [:svg {:xmlns "http://www.w3.org/2000/svg"
           :preserveAspectRatio "xMinYMin meet"
           :width 960 :height 400
           :viewBox "0 0 950 500"}

     [:g.states
      (unify data
             (fn [{:keys [state geo value]}]
               [:path.state {:name state
                             :d (geo->svg geo
                                          :projection proj)
                             :stroke "black"
                             :fill (color-scale value)}]))]])
