(ns scratch.binding
  (:use-macros [c2.util :only [p pp timeout abind!]])
  (:use [c2.core :only [unify]]))

(def !c (atom ["red"]))

(def co (abind! "#main"
                [:ol
                 (unify @!c (fn [color]
                              [:li {:style {:color color :-webkit-transition "all 2s ease-out"}} "list"]))]))
(timeout 1000
         (swap! !c #(conj % "blue")))
(timeout 2000
         (reset! !c ["purple" "red" "blue"]))

