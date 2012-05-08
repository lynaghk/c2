^:clj (ns c2.maths
        (:use [c2.util :only [combine-with]]))

^:cljs (ns c2.maths
         (:use-macros [c2.util :only [combine-with]]))


(def Pi 3.141592653589793)
(def Tau (* 2 Pi))
(def radians-per-degree (/ Pi 180))


(defn sin [x] (Math/sin x))
(defn cos [x] (Math/cos x))
(defn tan [x] (Math/tan x))

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

(defn irange
  "Inclusive range; same as core/range, but includes the end."
  ([start] (range start))
  ([start end] (concat (range start end) [end]))
  ([start end step] (concat (range start end step) [end])))

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
