(ns c2.scale
  (:use [c2.util :only [c2-obj]]
        [c2.maths :only [log10]]))

(defprotocol IScale
  (domain [this] [this x])
  (range [this] [this x]))

(defn linear [& {:keys [domain range]
                 :or {domain [0 1]
                      range  [0 1]}
                 :as args}]

  (let [domain-length (- (last domain) (first domain))
        range-length (- (last range) (first range))]

    (c2-obj linear
            IScale [domain range]
            clojure.lang.IFn (invoke [_ x] (+ (first range)
                                              (* range-length
                                                 (/ (- x (first domain))
                                                    domain-length)))))))

(defn log [& {:keys [domain range]
              :or {domain [1 10]
                   range  [0 1]}
              :as args}]
  (c2-obj log
          IScale [domain range]
          clojure.lang.IFn (invoke [_ x]
                                   (comp (linear :domain (map log10 domain)
                                                 :range range)
                                         log10))))


(comment
  (satisfies? IScale (linear))
  (let [a (linear)
        b (assoc a :domain [0 5])]
    ((juxt a b) 0.5))
  (assoc (linear) :domain [0 5])

  )

