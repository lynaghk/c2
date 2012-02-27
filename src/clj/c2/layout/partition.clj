(ns c2.layout.partition
  (:refer-clojure :exclude [partition]))


(defn partition [root & {:keys [sort children value size output-key]
                         :or {sort nil
                              children :children
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

  (use 'cheshire.core)
  (def flare (parse-string
              (slurp "../../../software/d3/examples/data/flare.json")
              true))


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



