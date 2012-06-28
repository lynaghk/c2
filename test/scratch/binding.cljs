(ns scratch.binding
  (:refer-clojure :exclude [+ - =])
  (:use-macros [c2.util :only [p pp timeout interval profile
                               abind!]])
  (:use [c2.core :only [unify]]
        [cassowary.core :only [cvar simplex-solver
                               constrain! stay! value
                               + - =]])
  (:require [c2.dom :as dom]))

;; (def !c (atom ["red"]))

;; (def co (abind! "#main"
;;                 [:ol
;;                  (unify @!c (fn [color]
;;                               [:li {:style {:color color :-webkit-transition "all 2s ease-out"}} "list"]))]))
;; (timeout 1000
;;          (swap! !c #(conj % "blue")))
;; (timeout 2000
;;          (reset! !c ["purple" "red" "blue"]))






;; (set! *print-fn* #(.log js/console %))
;; (profile "render/append"
;;          (dotimes [_ 1000]
;;            (dom/append! "#main" [:p "new dom" [:span {:style {:color "blue"}}
;;                                                "IMPLEMENTATION!"]])))

(extend-type Cl/Variable
  IHash
  (-hash [this] (goog.getUid this)))



(let [height 200, width 800, max-radius 40
      solver  (simplex-solver)
      spacing (cvar) ;;The spacing between circles (to be solved for)
      n 10
      circles (repeatedly n #(hash-map :r  (cvar (* max-radius (rand)))
                                       :cx (cvar 0)
                                       :cy (cvar (/ height 2))))]

  ;;The circle radii and vertical positions are constants
  (doseq [c circles]
    (stay! solver (:r c))
    (stay! solver (:cy c)))

  ;;Spacing between first circle and the wall
  (constrain! solver (= 0 (- (:cx (first circles))
                             (:r (first circles))
                             spacing)))

  ;;Spacing between each pair of neighboring circles
  (doseq [[left right] (partition 2 1 circles)]
    (constrain! solver (= spacing (- (:cx right)
                                     (:r right)
                                     (+ (:cx left) (:r left))))))

  ;;Spacing between last circle and the wall
  (constrain! solver (= spacing (- width
                                   (:cx (last circles))
                                   (:r (last circles)))))

  (let [!grayscales (atom (range 0 101 10))]
    ;;Draw the circles as SVG
    (abind! "body"
            [:svg {:width width :height height
                   :style {:border "1px solid black"
                           :margin "20px"}}
             (unify (map vector circles @!grayscales)
                    (fn [[c gray]]
                      [:circle {:style {:-webkit-transition "all 2s ease-out"
                                        :fill (str "hsl(0,0%," gray "%)")}
                                :cx (value (:cx c))
                                :cy (value (:cy c))
                                :r (value (:r c))}]))])

    (interval 2000
              (swap! !grayscales reverse))))
