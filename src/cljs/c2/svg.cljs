(ns c2.svg
  (:use [c2.core :only [translate unify!]])
  (:require [pinot.dom :as dom]
            [pinot.html :as html]))

(defn axis [selector scale & {:keys [ticks label label-sep]}]
    (unify! selector ticks
          (fn [d]
            [:svg:g.tick (translate (scale d) 0)
             [:svg:text {:y 30} (str d)]
             [:svg:line {:y1 0, :y2 10}]]))
    
    ;;Extra axis trimmings
    (let [container (first (dom/query selector))
          x1 (scale (first ticks))
          x2 (scale (last ticks))]
      
      ;;horizontal rule
      (dom/append container
                  (html/html [:svg:line.rule {:x1 x1
                                              :x2 x2}]))

      (when label
        (dom/append container
                    (html/html [:svg:text.label {:x (+ x1 (/ (- x2 x1) 2))
                                                 :y (or label-sep 50)} label])))))
