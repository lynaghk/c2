^:clj (ns c2.ticks
        (:use [c2.maths :only [sq ceil floor log10 expt]]
              [iterate :only [iter]]))

^:cljs (ns c2.ticks
         (:use-macros [iterate :only [iter]])
         (:use [c2.maths :only [sq ceil floor log10 expt]]))

;;Implementation of "An Extension of Wilkinson’s Algorithm for Positioning Tick Labels on Axes" by Justin Talbot, Sharon Lin, and Pat Hanrahan:
;;
;;    http://graphics.stanford.edu/vis/publications/2010/labeling-preprint.pdf
;;
;;see also: http://www.justintalbot.com/research/axis-labeling/


(def Q "Preference-ordered list of nice step sizes"
  [1 5 2 2.5 4 3])

(defn index-of "Index of x in coll"
  [x coll]
  (first (for [[idx y] (map-indexed vector coll)
               :when (= y x)] idx)))

(defn label-range-contains-zero? [l-min l-max l-step]
  (and (> l-max 0) (< l-min 0) (zero? (mod l-min l-step))))

(defn simplicity
  "Objective function modeling niceness of step sizes and whether a range includes zero."
  [q j label-range-contains-zero]
  (let [v (if label-range-contains-zero 1 0)]
    (if (<= (count Q) 1)
      (+ (- 1 j) v)
      (+ (- 1 (/ (index-of q Q) (dec (count Q))) j)
         v))))

(defn max-simplicity [q j] (simplicity q j true))

(defn coverage
  "Objective function based on distances between extreme data and extreme labels"
  [d-min d-max l-min l-max]
  (- 1 (* 0.5 (/ (+ (sq (- d-max l-max))
                    (sq (- d-min l-min)))
                 (sq (* 0.1 (- d-max d-min)))))))

(defn max-coverage
  "When the label range is centered on the data range"
  [d-min d-max span]
  (let [d-range (- d-max d-min)]
    (if (> span d-range)
      (- 1 (sq (/ (- span d-range)
                  (* 0.2 d-range))))
      1)))


(defn density
  "Objective function for a candidate density r and desired density rt (e.g. labels-per-cm)"
  [r rt]
  ;;Note the formula should be 2-, not 1- as in the paper.
  (- 2 (max (/ r rt) (/ rt r))))

;;Since the arguments are the same, I don't see what the story is with density / max density.
;;This isn't satisfactorily explained in the paper.
(defn max-density [r rt]
  (if (>= r rt)
    (- 2 (/ r rt))
    1))

(defn- w
  "Balance the relative merits of different metrics"
  [[simplicity coverage density legibility]]
  (let [w [0.2 0.25 0.5 0.05]]
    (+ (* simplicity (w 0))
       (* coverage (w 1))
       (* density (w 2))
       (* legibility (w 3)))))



(defn search
  "Find best ticks for the data range (d-min, d-max) and target label density.
Returns a map with {:min :max :step :extent :ticks} of optimal labeling, if one is found.
Returns an empty map if no labelings can be found.

target-density: labels per length; defaults to 0.01---one label per 100 units
length: available label spacing
"
  [[d-min d-max] & {:keys [target-density
                           length]
                    :or {target-density 0.01 ;;Default to one label per 100 px
                         length         500}}]

  ;;To be on the safe side, I've copied the imperative algorithm from the paper.
  ;;If you rewrite it in an understandable and performant functional style, we'll accept a pull request and buy you a bottle of whiskey.
  (let [best-score (atom -2)
        label (atom {})]

    (iter {for q in Q}

          (iter {for j from 1}
                {for ms = (max-simplicity q j)}
                {return-if (< (w [ms 1 1 1]) @best-score)}

                (iter {for k from 2}
                      {for md = (max-density (/ k length) target-density)}
                      {return-if (< (w [ms 1 md 1]) @best-score)}

                      (let [delta ;;power of ten by which to multiply the step size
                            (/ (- d-max d-min)
                               (* (inc k) j k))]
                        (iter {for z from (ceil (log10 delta))}
                              {for l-step = (* q j (expt 10 z))}
                              {for mc = (max-coverage d-min d-max (* (dec k) l-step))}
                              {return-if (< (w [ms mc md 1]) @best-score)}

                              (iter {for start
                                     from (- (floor (/ d-max l-step))
                                             (dec k))
                                     to (/ d-min l-step)
                                     by (/ 1 j)}
                                    {for l-min = (* start l-step)}
                                    {for l-max = (+ l-min (* (dec k) l-step))}
                                    {for s = (simplicity q j (label-range-contains-zero? l-min l-max l-step))}
                                    {for c = (coverage d-min d-max l-min l-max)}
                                    {for d = (density (/ k length) target-density)}
                                    {for score = (w [s c d 1])}
                                    {return-if (< score @best-score)}
                                    
                                    ;;(println "inner loop")
                                    ;;todo, optimize legibility
                                    
                                    (reset! best-score score)
                                    (reset! label {:min l-min
                                                   :max l-max
                                                   :step l-step})))))))
    (let [l @label]
      (assoc l
        :extent [(min (:min l) d-min)
                 (max (:max l) d-max)]
        :ticks (concat (range (:min l) (:max l) (:step l)) [(:max l)])))))


(comment
  (search [1 9])
  (search [1 9] :target-density (/ 1 20))
  )
