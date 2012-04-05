^:clj (ns c2.svg
        (:use [c2.maths :only [Pi Tau radians-per-degree
                               sin cos]]
              [clojure.core.match :only [match]]))

^:cljs (ns c2.svg
         (:use-macros [clojure.core.match.js :only [match]])
         (:use [c2.maths :only [Pi Tau radians-per-degree
                                sin cos]]))


;;Lil' SVG helpers
(defn translate [coordinates]
  (match [coordinates]
         [[x y]] (str "translate(" x "," y ")")
         [{:x x :y y}] (recur [x y])))

(defn scale [coordinates]
  (match [coordinates]
         [[x y]] (str "scale(" x "," y ")")
         [{:x x :y y}] (recur [x y])))


(defn axis
  "Returns axis <g> for input scale with ticks.
Direction away from the data frame is defined to be positive; use negative margins and widths for the axis to render inside of the data frame"
  [scale ticks & {:keys [orientation
                         formatter
                         major-tick-width
                         text-margin]
                  :or {orientation :left
                       formatter str
                       major-tick-width 6
                       text-margin 9}}]

  (let [[x y x1 x2 y1 y2] (match [orientation]
                                 [(:or :left :right)] [:x :y :x1 :x2 :y1 :y2]
                                 [(:or :top :bottom)] [:y :x :y1 :y2 :x1 :x2])

        parity (match [orientation]
                      [(:or :left :top)] -1
                      [(:or :right :bottom)] 1)]

    (into [:g.axis {:class (name orientation)}
           [:line.rule (apply hash-map (interleave [y1 y2] (:range scale)))]]
           (map (fn [d]
            [:g.major-tick {:transform (translate {x 0 y (scale d)})}
             [:text {x (* parity text-margin)} (formatter d)]
             [:line {x1 0 x2 (* parity major-tick-width)}]])
          ticks))))


(def ArcMax (- Tau 0.0000001))

(defn circle
  "Returns svg path data for a circle starting at 3 o'clock and sweeping in positive y."
  ([radius] (circle [0 0] radius))
  ([[x y] radius]
     (str "M"  (+ x radius) "," y
          "A" (+ x radius) "," (+ y radius) " 0 1,1" (- (+ x radius)) "," y
          "A" (+ x radius) "," (+ y radius) " 0 1,1" (+ x radius) "," y)))

(defn arc
  [& {:keys [inner-radius, outer-radius
             start-angle, end-angle, angle-offset]
      :or {inner-radius 0, outer-radius 1
           start-angle 0, end-angle Pi, angle-offset 0}}]
  (let [r0 inner-radius
        r1 outer-radius
        [a0 a1]  (sort [(+ angle-offset start-angle)
                        (+ angle-offset end-angle)])
        da (- a1 a0)
        large-arc-flag (if (< da Pi) "0" "1")

        s0 (sin a0), c0 (cos a0)
        s1 (sin a1), c1 (cos a1)]

    ;;SVG "A" parameters: (rx ry x-axis-rotation large-arc-flag sweep-flag x y)
    ;;see http://www.w3.org/TR/SVG/paths.html#PathData
    (if (>= da ArcMax)
      ;;Then just draw a full annulus
      (str "M0," r1
           "A" r1 "," r1 " 0 1,1 0," (- r1)
           "A" r1 "," r1 " 0 1,1 0," r1
           (if (not= 0 r0) ;;draw inner arc
             (str "M0," r0
                  "A" r0 "," r0 " 0 1,0 0," (- r0)
                  "A" r0 "," r0 " 0 1,0 0," r0))
           "Z")

      ;;Otherwise, draw the wedge
      (str "M" (* r1 c0) "," (* r1 s0)
           "A" r1 "," r1 " 0 " large-arc-flag ",1 " (* r1 c1) "," (* r1 s1)
           (if (not= 0 r0) ;;draw inner arc
             (str "L" (* r0 c1) "," (* r0 s1)
                  "A" r0 "," r0 " 0 " large-arc-flag ",0 " (* r0 c0) "," (* r0 s0))
             "L0,0")
           "Z"))))
