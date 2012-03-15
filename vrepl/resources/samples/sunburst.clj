(ns sunburst
  (:refer-clojure :exclude [partition]) ;;avoid name conflict with base "partition" function
  (:use [c2.core :only [unify style]]
        [c2.maths :only [sqrt sin cos Tau]] ;;Life's short, don't waste it writing 2*Pi
        [c2.svg :only [arc]]
        [c2.layout.partition :only [partition]]
        
        [vomnibus.d3 :only [flare]]))

(let [radius 170
      ;;Partition will give us entries for every node;
      ;;we only want slices, so filter out the root node.
      slices (filter #(-> % :partition :depth (not= 0)) 
                     (partition flare :value :size
                                :size [Tau (* radius radius)]))]
  
  [:svg {:width (* 2 radius) :height (* 2 radius)}
   [:g {:transform (str "translate(" radius "," radius ")")}
    (unify slices
     (fn [{name :name, bites :bites
          {:keys [x dx y dy]} :partition}]
       [:g.slice
        [:path {:d (arc :inner-radius (sqrt y)
                        :outer-radius (sqrt (+ y dy))
                        :start-angle x
                        :end-angle  (+ x dx))}]]))]]
)
 
