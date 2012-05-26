(ns scratch.binding
  (:use-macros [c2.util :only [p pp timeout]]
               [reflex.macros :only [computed-observable capture-derefed]])
  (:require [c2.dom :as dom]
            ;;required because reflex macros reference this ns.
            [reflex.core :as reflex])
  (:use [singult.core :only [render merge! unify]]))

(defn bind! [parent el-fn]
  (let [co (computed-observable (el-fn))
        $e (dom/append! parent (render @co))]
    
    (add-watch co :update-dom #(merge! $e @co))
  
    co))




(def !c (atom ["red"]))

(def co (bind! "#main" (fn []
                         [:ol
                          (unify @!c (fn [color]
                                       [:li {:style {:color color :-webkit-transition "all 2s ease-out"}} "list"]))])))
(timeout 1000
         (swap! !c #(conj % "blue")))
(timeout 2000
         (reset! !c ["purple" "red" "blue"]))

