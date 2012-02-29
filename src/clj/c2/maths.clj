(ns c2.maths)

;;How can we put together math functions to use Java vs. JavaScript math functions automagically?

(def Pi 3.141592653589793)
(def Tau (* 2 Pi))
(def radians-per-degree (/ Pi 180))


(defn sin [x] (Math/sin x))
(defn cos [x] (Math/cos x))
(defn expt
  ([x] (Math/exp x))
  ([x y] (Math/pow x y)))
(defn sqrt [x] (Math/sqrt x))
(defn abs [x] (Math/abs x))


(defn percentage [x y]
  (str (* 100 (/ x y)) "%"))



;;element-by-element arithmetic
;;Code modified from Incanter
;;TODO move macros to their own namespace for CLJS compatability
(defmacro combine-with [A B op fun]
  `(cond
    (and (number? ~A) (number? ~B)) (~op ~A ~B)
    (and (coll? ~A) (coll? ~B)) (map ~op ~A ~B)
    (and (number? ~A) (coll? ~B)) (map ~op (replicate (count ~B) ~A)  ~B)
    (and (coll? ~A) (number? ~B)) (map ~op ~A (replicate (count ~A) ~B))))

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

;;CLJS!
(comment
  (def sin (.-sin js/Math))
  (def cos (.-cos js/Math))
  (def expt (.-pow js/Math))
  (def sqrt (.-sqrt js/Math))
  )
