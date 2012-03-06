(ns c2.svg
  (:use [c2.maths :only [Pi Tau radians-per-degree
                         sin cos]]))

(def ArcMax (- (* 2 Pi) 0.0000001))

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
