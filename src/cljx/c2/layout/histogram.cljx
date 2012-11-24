;; The histogram layout transforms data by grouping descrete data points into
;; bins.
;;
;; (hist/histogram [{:name "sally" :age 20}
;;                  {:name "al" :age 55}
;;                  {:name "ali" :age 56}
;;                  {:name "amanda" :age 12}
;;                  {:name "andy" :age 26}
;;                  {:name "brock" :age 30}]
;;                 :value :age
;;                 :range [13 30]
;;                 :bins 3)

(ns c2.layout.histogram
  (:use [c2.maths :only [log]]))


(defn- binary-search [v target]
  "VM agnostic j.u.Collections/binarySearch, from http://www.gettingclojure.com/cookbook:sequences"
  (loop [low 0
         high (dec (count v))]
    (if (> low high)
      (- (inc low))
      (let [mid (quot (+ low high) 2)
            mid-val (v mid)]
        (cond (< mid-val target) (recur (inc mid) high)
              (< target mid-val) (recur low (dec mid))
              :else mid)))))

(defn- sturges
  "Calulate reasonable number of bins assuming an approximately normal
  distribution"
  [values]
  (-> (count values)
    (log 2)
    int
    inc))

(defn- fixed-size-bins
  "Return the inclusive upper threshold of all bins"
  [[mn mx] n]
  (let [size (/ (- mx mn) n)]
    (vec (for [step (range (inc n))]
           (+ mn (* size step))))))

(defn histogram
  "The histogram layout transforms data by grouping descrete data points into
  bins. Returns a collection of values with the following metadata set:

  > *:x* the lower bound of the bin (inclusive).

  > *:dx* the width of the bin; x + dx is the upper bound (exclusive).

  > *:y* the count

   Kwargs:

   > *:value* fn that calculates value of node, defaults to `:value`

   > *:index* opaque value passed to `:bins` and `:range`

   > *:range* fn which calculates the minimum and maximum values given the array of values

   > *:bins* Number of bins /or/ fn that takes the range, array of values, and current index"
  [data & {:keys [value index range bins]
           :or {value :value
                range (fn [xs _] ((juxt (partial reduce min)
                                        (partial reduce max))
                                    xs))
                bins (fn [r xs _] (fixed-size-bins r (sturges xs)))}}]

  (let [values (map value data)
        r (cond
            (fn? range) (range values index)
            :else range)
        thresholds (cond
                     (fn? bins) (bins r values index)
                     (number? bins) (fixed-size-bins r bins)
                     :else bins)
        binner (fn [e] (->> (value e)
                                        ; remove trailing threshold number
                         (binary-search (subvec thresholds 0 (dec (count thresholds))))
                         inc
                         Math/abs
                         dec))
        groups (group-by binner
                         (filter #(and (>= (value %) (r 0))
                                       (<= (value %) (r 1)))
                                 data))]

    (map-indexed (fn [index [mn mx]]
                   (let [group (or (groups index)
                                   [])]
                     (with-meta group
                                {:x mn
                                 :dx (- mx mn)
                                 :y (count group)})))
                 (partition 2 1 thresholds))))
