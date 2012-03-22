(ns sunburst
  (:refer-clojure :exclude [partition]) ;;avoid name conflict with base "partition" function
  (:use [c2.core :only [unify style]]
        [c2.maths :only [sqrt sin cos Tau]] ;;Life's short, don't waste it writing 2*Pi
        [c2.svg :only [arc]]
        [c2.layout.partition :only [partition]]
        
        [vomnibus.d3 :only [flare]]
        [vomnibus.d3-color :only [Categorical-20]]))

(let [radius 270
      ;;Partition will give us entries for every node;
      ;;we only want slices, so filter out the root node.
      slices (filter #(-> % :partition :depth (not= 0)) 
                     (partition flare :value :size
                                :size [Tau (* radius radius)]))
      ;;TODO: Rework partition to include reference to parent so we can match D3's coloring scheme?
      color-scale (apply hash-map (interleave
                                   (into #{} (map :name (filter :children slices)))
                                   (flatten (repeat Categorical-20))))]

  [:svg {:width (* 2 radius) :height (* 2 radius)}
   [:g {:transform (str "translate(" radius "," radius ")")}
    (unify slices
     (fn [{name :name, bites :bites
          {:keys [x dx y dy]} :partition}]
       [:g.slice
        [:path {:title name
                :d (arc :inner-radius (sqrt y)
                        :outer-radius (sqrt (+ y dy))
                        :start-angle x
                        :end-angle  (+ x dx))
                :fill "white"}]]))]]
)
