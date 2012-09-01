^:clj (ns c2.maths
        (:use [c2.util :only [combine-with]]))

^:cljs (ns c2.maths
         (:use-macros [c2.util :only [combine-with]]))


(def Pi Math/PI)
(def Tau (* 2 Pi))
(def e Math/E)
(def radians-per-degree (/ Pi 180))
(defn rad [x] (* radians-per-degree x))
(defn deg [x] (/ x radians-per-degree))


(defn sin [x] (Math/sin x))
(defn asin [x] (Math/asin x))
(defn cos [x] (Math/cos x))
(defn acos [x] (Math/acos x))
(defn tan [x] (Math/tan x))
(defn atan [x] (Math/atan x))

(defn expt
  ([x] (Math/exp x))
  ([x y] (Math/pow x y)))

(defn sq [x] (expt x 2))
(defn sqrt [x] (Math/sqrt x))

(defn floor [x] (Math/floor x))
(defn ceil [x] (Math/ceil x))
(defn abs [x] (Math/abs x))

(defn log
  ([x] (Math/log x))
  ([base x] (/ (Math/log x)
               (Math/log base))))

(defn ^:clj log10 [x] (Math/log10 x))
(defn ^:cljs log10 [x] (/ (.log js/Math x)
                          (.-LN10 js/Math)))


(defn extent
  "Returns 2-vector of min and max elements in xs."
  [xs]
  [(apply min xs)
   (apply max xs)])


;;TODO: replace mean and median with smarter algorithms for better performance.
(defn mean
  "Arithemetic mean of collection"
  [xs]
  (/ (reduce + xs)
     (count xs)))

(defn median
  "Median of a collection."
  [xs]
  (let [sorted (sort xs)
        n (count xs)]
    (cond
     (= n 1)  (first sorted)
     (odd? n) (nth sorted (/ (inc n) 2))
     :else    (let [mid (/ n 2)]
                (mean [(nth sorted (floor mid))
                       (nth sorted (ceil mid))])))))

(defn irange
  "Inclusive range; same as core/range, but includes the end."
  ([start] (range start))
  ([start end] (concat (range start end) [end]))
  ([start end step]
     (let [r (range start end step)]
       (if (== (mod (first r) step)
               (mod end step))
         (concat r [end])
         r))))

(defn within?
  "Checks if bottom <= x <= top."
  [x [bottom top]]
  (<= bottom x top))


;;element-by-element arithmetic
;;Code modified from Incanter
(defn add
  ([& args] (reduce (fn [A B] (combine-with A B clojure.core/+ add)) args)))
(defn sub
  ([& args] (if (= (count args) 1)
              (combine-with 0 (first args) clojure.core/- sub)
              (reduce (fn [A B] (combine-with A B clojure.core/- sub)) args))))
(defn mul
  ([& args] (reduce (fn [A B] (combine-with A B clojure.core/* mul)) args)))
(defn div
  ([& args] (if (= (count args) 1)
              (combine-with 1 (first args) clojure.core// div)
              (reduce (fn [A B] (combine-with A B clojure.core// div)) args))))

(defn quantile
  "Returns the quantiles of a dataset.

   Kwargs:

     > *:probs*: ntiles of the data to return, defaults to `[0 0.25 0.5 0.75 1]`

  Algorithm is the same as R's quantile type=7.
  Transcribed from Jason Davies; https://github.com/jasondavies/science.js/blob/master/src/stats/quantiles.js"
  [data & {:keys [probs] :or {probs [0 0.25 0.5 0.75 1]}}]
  (let [xs (into [] (sort data))
        n-1 (dec (count xs))]
    (for [q probs]
      (let [index (inc (* q n-1))
            lo    (int (floor index))
            h     (- index lo)
            a     (xs (dec lo))]
        (if (= h 0)
          a
          (+ a (* h (- (xs lo) a))))))))
