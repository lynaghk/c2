(ns pie
  (:refer-clojure :exclude [partition]) ;;avoid name conflict with base "partition" function
  (:use [c2.core :only [unify style]]
        [c2.maths :only [sin cos Tau]] ;;Life's short, don't waste it writing 2*Pi
        [c2.svg :only [arc]]
        [c2.layout.partition :only [partition]]))


(let [data {:name "Delicious Pie"
           :children [{:name "Eaten" :bites 11}
                      {:name "Not Eaten" :bites 39}
                      {:name "Silverware" :bites 8}]}

      ;;Partition will give us entries for every node, but we only want slices.
      slices (filter #(-> % :partition :depth (= 1)) 
                     (partition data :value :bites, :size [Tau 1]))
      radius 170]
  
  [:svg
   [:g {:transform "translate(200,200)"}
    (unify slices
     (fn [{name :name, bites :bites
          {:keys [x dx]} :partition}]
       [:g.slice
        [:path {:d (arc :outer-radius radius
                        :start-angle x
                        :end-angle  (+ x dx))
                
                ;;Style inline if you want, or use classes+CSS
                :style (style {:stroke "black"
                               :fill (case name
                                       "Eaten"      "yellow"
                                       "Not Eaten"  "tan"
                                       "Silverware" "silver")})}]
        ;;Make a label.
        ;;SVG doesn't support radial coordinates, so this is a bit ugly...
        (let [label-angle (+ x (/ dx 2))]
          [:text {:x (* 0.5 radius (cos label-angle))
                  :y (* 0.5 radius (sin label-angle))
                  :text-anchor (condp > (mod label-angle Tau)
                                 (* Tau 0.25) "start"
                                 (* Tau 0.75) "end"
                                 Tau "start")}
           (str name)])]))]])
