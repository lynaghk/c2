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

;;CLJS!
(comment
  (def sin (.-sin js/Math))
  (def cos (.-cos js/Math))
  (def expt (.-pow js/Math))
  (def sqrt (.-sqrt js/Math))
)
