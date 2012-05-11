(ns boxplot
  (:use [c2.core :only [unify]]
        [c2.maths :only [sin cos Tau extent]]
        [c2.svg :only [translate]]
        [clojure.string :only [join]])
  (:require [c2.scale :as scale]))

(let [height 400
      width 960
      group-width 30
      box-width 20
      data (repeatedly (/ width group-width)
                       #(into {} (map vector
                                      [:min :q5 :q10 :q25 :median :q75 :q90 :q95 :max]
                                      (sort (take 9 (repeatedly rand))))))
      s (scale/linear :domain (extent (flatten (map vals data)))
                      ;;Note the range is [height, 0]; since in SVG the origin is at the top of the element.
                      :range [height 0])]

  (defn box-width-line [y-position]
    [:line {:x1 0 :x2 box-width
            :y1 y-position :y2 y-position}])

  [:svg#main {:style (str "display: block;"
                          "margin: auto;"
                          "height:" height ";"
                          "width:" width ";")}

   [:style {:type "text/css"}
    (join "\n" [ "<![CDATA["
                 ".box {fill: #222222; stroke: white; }"
                 "line {stroke: white;}"
                 "line.range {stroke-dasharray: 5,5;}"
                 "]]>"])]

   (unify (map-indexed vector data)
          (fn [[idx {:keys [min q10 q25 median q75 q90 max]}]]
            [:g.boxplot {:transform (translate [(* idx group-width) 0])}

             ;;line spanning the 10th and 90th percentile
             [:g.range
              [:line.range {:x1 (* 0.5 box-width) :x2 (* 0.5 box-width)
                            :y1 (s q10) :y2 (s q90)}]
              (box-width-line (s q10))
              (box-width-line (s q90))]

             ;;box from the 25th to 75th percentile
             [:rect.box {:x 0 :y (s q75)
                         :height (- (s q25) (s q75)) :width box-width}]

             (box-width-line (s median))]))])
