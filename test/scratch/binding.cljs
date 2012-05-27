(ns scratch.binding
  (:use-macros [c2.util :only [p pp timeout abind! profile]])
  (:use [c2.core :only [unify]])
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
