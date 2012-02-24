(ns c2.geo.projection
  (:use [c2.maths :only [radians-per-degree
                         sin cos sqrt]]))

(defn albers [& {:keys [origin parallels scale translate]
                 :or {origin [-98 38]
                      parallels [29.5, 45.5]
                      scale 1000
                      translate [480 250]}}]

  (let [phi1 (* radians-per-degree (first parallels))
        phi2 (* radians-per-degree (second parallels))
        lng0 (* radians-per-degree (first origin))
        lat0 (* radians-per-degree (second origin))

        s (sin phi1), c (cos phi1)
        n (* 0.5 (+ s (sin phi2)))
        C (+ (* c c) (* 2 n s))
        p0 (/ (sqrt (- C (* 2 n (sin lat0)))) n)]

    (fn [[x y]]
      (let [t (* n (- (* radians-per-degree x)
                      lng0))
            p (/ (sqrt (- C (* 2 n (sin (* radians-per-degree y)))))
                 n)]
        [(+ (* scale p (sin t)) (first translate))
         (+ (* scale (- (* p (cos t)) p0))
            (second translate))]))))
