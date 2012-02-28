(ns c2.geom.t-polygon
  (:use midje.sweet
        [c2.geom.polygon :only [centroid area]]))

(let [closed-ccw-square [[0 0] [0 1] [1 1] [1 0] [0 0]]
      closed-cw-square (reverse closed-ccw-square)
      open-ccw-square [[0 0] [0 1] [1 1] [1 0]]
      open-cw-square (reverse open-ccw-square)

      closed-cw-triangle [[1 1] [3 2] [2 3] [1 1]]
      open-cw-triangle [[1 1] [3 2] [2 3]]]

  (tabular
   (facts "Centroid"
          (centroid ?coordinates) => ?expected)
   ?coordinates         ?expected
   closed-ccw-square    [0.5 0.5]
   closed-cw-square     [0.5 0.5]
   open-ccw-square      [0.5 0.5]
   open-cw-square       [0.5 0.5]
   closed-cw-triangle   [2.0 2.0]
   open-cw-triangle     [2.0 2.0])

  (tabular
   (facts "Area"
          (area ?coordinates) => (roughly ?expected))
   ?coordinates              ?expected
   closed-ccw-square         1
   closed-cw-square         -1
   open-ccw-square           1
   open-cw-square           -1
   closed-cw-triangle       -1.5
   open-cw-triangle         -1.5))
