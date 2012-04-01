^:clj (ns c2.scale
        (:use [c2.util :only [c2-obj]]
              [c2.maths :only [log10]]))
^:cljs (ns c2.scale
         (:use-macros [c2.util :only [c2-obj]])
         (:use [c2.maths :only [log10]]))

(c2-obj linear {:domain [0 1]
                :range  [0 1]}

        clojure.lang.IFn
        (invoke [_ x] (let [domain-length (- (last domain) (first domain))
                            range-length (- (last range) (first range))]
                        (+ (first range)
                                         (* range-length
                                            (/ (- x (first domain))
                                               domain-length))))))

(c2-obj log {:domain [1 10]
             :range  [0 1]}
        clojure.lang.IFn
        (invoke [_ x]
                ((comp (linear :domain (map log10 domain)
                                :range range)
                        log10) x)))





