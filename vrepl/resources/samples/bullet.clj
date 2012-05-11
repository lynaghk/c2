(ns bullet
  (:use [c2.core :only [unify]])
  (:require [c2.scale :as scale]
            [vomnibus.color-brewer :as color-brewer]))


(def css "
.bullet { font: 10px sans-serif; }
.bullet .labels { fill: white; text-anchor: end; }
.bullet .marker { stroke: #000; stroke-width: 2px; }
.bullet .tick line { stroke: #666; stroke-width: .5px; }
.bullet .range.s0 { fill: #eee; }
.bullet .range.s1 { fill: #ddd; }
.bullet .range.s2 { fill: #ccc; }
.bullet .measure.s0 { fill: lightsteelblue; }
.bullet .measure.s1 { fill: steelblue; }
.bullet .title { font-size: 14px; font-weight: bold; }
.bullet .subtitle { fill: #999; }
")


(let [data [{:title "Revenue" :subtitle "USD in thousands" :ranges [150 225 300] :measures [220 270] :markers [250]}
            {:title "Profit" :subtitle "%" :ranges [20 25 30] :measures [21 23] :markers [26]}
            {:title "Order Size" :subtitle "USD average" :ranges [350 500 600] :measures [100 320] :markers [550]}
            {:title "New Customers" :subtitle "count" :ranges [1400 2000 2500] :measures [1000 1650] :markers [2100]}
            {:title "Satisfaction" :subtitle "out of 5" :ranges [3.5 4.25 5] :measures [3.2 4.7] :markers [4.4]}]]

  
  (unify data
         (fn [{:keys [title subtitle ranges measures markers]}]
           (let [bar-width 800
                 range-height 25
                 measure-height 9
                 marker-height 15
                 label-margin 120
                 s (scale/linear :domain [0 (apply max (flatten [ranges measures markers]))]
                                 :range [0 bar-width])]
             
             [:svg.bullet {:xmlns "http://www.w3.org/2000/svg" :width 960 :height 40}
              [:style {:type "text/css"} (str "<![CDATA[" css "]]>")]
              
              ;;Text labels
              [:g.labels {:transform (str "translate(" (- label-margin 5) "," 12  ")")}
               [:text.title title]
               [:text.subtitle {:dy "1.2em"} subtitle]]
              
              ;;Rects; move to the right to make room for labels
              [:g.rects {:transform (str "translate(" label-margin ", 0)")}
               ;;Range rects
               (map (fn [[idx r]]
                      [:rect {:class (str "range s" idx)
                              :height range-height, :width (s r)}])
                    (map-indexed vector (sort > ranges)))

               ;;Measure rects
               (map (fn [[idx m]]
                      [:rect {:class (str "measure s" idx)
                              :height measure-height, :width (s m)
                              :y (* 0.5 (- range-height measure-height))}])
                    (map-indexed vector (sort > measures)))

               ;;Markers
               (map (fn [[idx m]]
                      [:rect.marker {:height marker-height, :width 2
                                     :x (s m), :y (* 0.5 (- range-height marker-height))}])
                    (map-indexed vector (sort > markers)))]
              
              ]))))
