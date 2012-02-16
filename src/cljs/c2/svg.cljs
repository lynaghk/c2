(ns c2.svg
  (:use [c2.core :only [translate unify!]])
  (:require [c2.dom :as dom]))

(defn axis [container scale & {:keys [ticks label label-sep]}]
  (unify! container ticks
          (fn [d]
            [:svg:g.tick (translate (scale d) 0)
             [:svg:text {:y 30} (str d)]
             [:svg:line {:y1 0, :y2 10}]]))

  ;;Extra axis trimmings
  (let [container (dom/select container)
        x1 (scale (first ticks))
        x2 (scale (last ticks))]
    
    ;;horizontal rule
    (dom/append! container
                [:svg:line.rule {:x1 x1
                                 :x2 x2}])

    (when label
      (dom/append! container
                  [:svg:text.label {:x (+ x1 (/ (- x2 x1) 2))
                                    :y (or label-sep 50)} label]))))
