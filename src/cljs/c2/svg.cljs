(ns c2.svg
  (:use [c2.core :only [translate unify!]])
  (:require [pinot.dom :as dom]
            [pinot.html :as html]))

(defn axis [selector scale & {:keys [ticks]}]
    (unify! selector ticks
          (fn [d]
            [:svg:g.tick (translate (scale d) 0)
             [:svg:text {:y 30} (str d)]
             [:svg:line {:y1 0, :y2 10}]]))
    
  ;;horizontal rule
  (dom/append (first (dom/query selector))
              (html/html [:svg:line.rule {:x1 (scale (first ticks))
                                          :x2 (scale (last ticks))}])))
