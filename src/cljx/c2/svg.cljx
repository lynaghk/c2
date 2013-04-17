;;Collection of helpers for dealing with scalable vector graphics.
;;
;;Coordinates to any fn can be 2-vector `[x y]` or map `{:x x :y y}`.
^:clj (ns c2.svg
        (:use [c2.core :only [unify]]
              [c2.maths :only [Pi Tau radians-per-degree
                               sin cos mean]]))

^:cljs (ns c2.svg
         (:use [c2.core :only [unify]]
               [c2.maths :only [Pi Tau radians-per-degree
                                sin cos mean]])
         (:require [c2.dom :as dom]))

;;Stub for float fn, which does not exist on cljs runtime
^:cljs (def float identity)

(defn ->xy
  "Convert coordinates (potentially map of `{:x :y}`) to 2-vector."
  [coordinates]
  (cond
   (and (vector? coordinates) (= 2 (count coordinates))) coordinates
   (map? coordinates) [(:x coordinates) (:y coordinates)]))

(defn translate [coordinates]
  (let [[x y] (->xy coordinates)]
    (str "translate(" (float x) "," (float y) ")")))

(defn scale [coordinates]
  (if (number? coordinates)
    (str "scale(" (float coordinates) ")")
    (let [[x y] (->xy coordinates)]
      (str "scale(" (float x) "," (float y) ")"))))

(defn rotate
  ([angle] (rotate angle [0 0]))
  ([angle coordinates]
     (let [[x y] (->xy coordinates)]
       (str "rotate(" (float angle) "," (float x) "," (float y) ")"))))


(defn ^:cljs get-bounds
  "Returns map of `{:x :y :width :height}` containing SVG element bounding box.
   All coordinates are in userspace. Ref [SVG spec](http://www.w3.org/TR/SVG/types.html#InterfaceSVGLocatable)"
  [$svg-el]
  (let [b (.getBBox $svg-el)]
    {:x (.-x b)
     :y (.-y b)
     :width (.-width b)
     :height (.-height b)}))

(defn transform-to-center
  "Returns a transform string that will scale and center provided element `{:width :height :x :y}` within container `{:width :height}`."
  [element container]
  (let [{ew :width eh :height x :x y :y} element
        {w :width h :height} container
        s (min (/ h eh) (/ w ew))]
    (str (translate [(- (/ w 2) (* s (/ ew 2)))
                     (- (/ h 2) (* s (/ eh 2)))]);;translate scaled to center
         " " (scale s) ;;scale
         " " (translate [(- x) (- y)]) ;;translate to origin
         )))


(defn ^:cljs transform-to-center!
  "Scales and centers `$svg-el` within its parent SVG container.
   Uses parent's width and height attributes only."
  [$svg-el]
  (let [$svg (.-ownerSVGElement $svg-el)
        t (transform-to-center (get-bounds $svg-el)
                               {:width (js/parseFloat (dom/attr $svg :width))
                                :height (js/parseFloat (dom/attr $svg :height))})]
    (dom/attr $svg-el :transform t)))



(defn axis
  "Returns axis <g> hiccup vector for provided input `scale` and collection of `ticks` (numbers).
   Direction away from the data frame is defined to be positive; use negative margins and widths to render axis inside of data frame.

   Kwargs:

   > *:orientation* &in; (`:top`, `:bottom`, `:left`, `:right`), where the axis should be relative to the data frame, defaults to `:left`

   > *:formatter* fn run on tick values, defaults to `str`

   > *:major-tick-width* width of ticks (minor ticks not yet implemented), defaults to 6

   > *:text-margin* distance between axis and start of text, defaults to 9

   > *:label* axis label, centered on axis; :left and :right orientation labels are rotated by +/- pi/2, respectively

   > *:label-margin* distance between axis and label, defaults to 28"
  [scale ticks & {:keys [orientation
                         formatter
                         major-tick-width
                         text-margin
                         label
                         label-margin]
                  :or {orientation :left
                       formatter str
                       major-tick-width 6
                       text-margin 9
                       label-margin 28}}]

  (let [[x y x1 x2 y1 y2] (case orientation
                            (:left :right) [:x :y :x1 :x2 :y1 :y2]
                            (:top :bottom) [:y :x :y1 :y2 :x1 :x2])

        parity (case orientation
                 (:left :top) -1
                 (:right :bottom) 1)]

    [:g {:class (str "axis " (name orientation))}
     [:line.rule (apply hash-map (interleave [y1 y2] (:range scale)))]
     [:g.ticks
      ;;Need to weave scale into tick stream so that unify updates nodes when the scale changes.
      (unify (map vector ticks (repeat scale))
             (fn [[d scale]]
               [:g.tick.major-tick {:transform (translate {x 0 y (scale d)})}
                [:text {x (* parity text-margin)} (formatter d)]
                [:line {x1 0 x2 (* parity major-tick-width)}]]))]

     (when label
       [:text.label {:transform (str (translate {x (* parity label-margin)
                                                 y (mean (:range scale))})
                                     " "
                                     (case orientation
                                       :left (rotate -90)
                                       :right (rotate 90)
                                       ""))}
        label])
     ]))


(def ArcMax (- Tau 0.0000001))

(defn circle
  "Calculate SVG path data for a circle of `radius` starting at 3 o'clock and sweeping in positive y."
  ([radius] (circle [0 0] radius))
  ([coordinates radius]
     (let [[x y] (->xy coordinates)]
       (str "M"  (+ x radius) "," y
            "A" (+ x radius) "," (+ y radius) " 0 1,1" (- (+ x radius)) "," y
            "A" (+ x radius) "," (+ y radius) " 0 1,1" (+ x radius) "," y))))

(defn arc
  "Calculate SVG path data for an arc."
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
