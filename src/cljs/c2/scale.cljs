(ns c2.scale)

(defn linear [& {:keys [domain range]
                 :or {domain [0 1]
                      range  [0 1]}}]
  (let [domain-length (- (last domain) (first domain))
        range-length (- (last range) (first range))]
    (fn [x]
      (+ (first range)
         (* range-length
            (/ (- x (first domain))
               domain-length))))))
