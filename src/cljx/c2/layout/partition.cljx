;; The partition layout transforms root node of hierarchy into a flat collection of nodes positioned and sized according to provided value fn.
;; E.g., a doughnut plot can be created by partitioning the angular width and radius of a circle:
;;
;;     (partition {:name "Pie"
;;                 :slices [{:name "Big slice" :val 5}
;;                          {:name "Lil' slice" :val 3}]}
;;                 :size [Tau 1]
;;                 :value :val
;;                 :children :slices) ;;=>
;;
;;     (;;Centre piece; circle with radius 1/2.
;;      {:partition {:dy 1/2, :dx 6.283185307179586, :y 0N, :x 0, :value 8, :depth 0}, :name "Pie", :slices [{:name "Big slice", :val 5} {:name "Lil' slice", :val 3}]}
;;
;;      ;;Outer pieces; radii from 1/2 to 1 and angular displacement given according to val
;;      {:partition {:dy 1/2, :dx 3.9269908169872414, :y 1/2, :x 0, :value 5, :depth 1}, :name "Big slice", :val 5}
;;      {:partition {:dy 1/2, :dx 2.356194490192345, :y 1/2, :x 3.9269908169872414, :value 3, :depth 1}, :name "Lil' slice", :val 3})

(ns c2.layout.partition
  (:refer-clojure :exclude [partition]))


(defn partition
  "Transforms `root` node of hierarchy into a flat collection of nodes positioned and sized according to provided value fn.

   Kwargs:

   > *:children* fn that calculates children of node, defaults to `:children`

   > *:value* fn that calculates value of node, defaults to `:value`

   > *:size* 2D space to be partitioned, defaults to `[1, 1]`

   > *:output-key* keyword added to node map in output collection that holds calculated positions, defaults to `:partition`"
  [root & {:keys [children value size output-key]
           :or {children :children
                value :value
                size [1 1]
                output-key :partition}}]

  (defn depth [node]
    (inc (if-let [cs (children node)]
           (apply max (map depth cs))
           0)))

  (defn node-value [node]
    (if-let [cs (children node)]
      (apply + (map node-value cs))
      (value node)))

  (defn position [node depth x [dx dy]]
    (concat
     ;;parent node
     [(assoc node output-key (merge (output-key node)
                                    {:depth depth
                                     :value (node-value node)
                                     :x x, :y (* depth dy)
                                     :dx dx, :dy dy}))]
     ;;child nodes
     (let [unit-cdx (/ dx (node-value node))
           cs (children node)]
       (flatten
        (map (fn [child cx]
               (position child (inc depth) cx [(* unit-cdx (node-value child)) dy]))
             cs
             ;;Calculate each child's x-offset
             (reductions (fn [cx child] (+ cx (* unit-cdx (node-value child))))
                         x cs))))))

  (position root 0 0 [(first size) (/ (second size) (depth root))]))


(comment
  (use '[vomnibus.d3 :only [flare]])
  (use '[c2.maths :only [Tau]])
  (partition flare :value #(do % 1))

  (partition {:name "rrr"
              :children [ {:name "A"
                           :children [{:name "a" :value 1}
                                      {:name "b" :value 1}
                                      {:name "c" :value 1}]}
                          {:name "B"
                           :children [{:name "ba" :value 2}
                                      {:name "bb" :value 2}
                                      {:name "bc" :value 2}]}]}))
