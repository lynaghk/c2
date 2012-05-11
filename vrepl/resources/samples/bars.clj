(ns bars
  (:use [c2.core :only [unify]])
  (:require [c2.scale :as scale]))


(let [width 600, bar-height 20
      data {"A" 1, "B" 2, "C" 4, "D" 3}
      s (scale/linear :domain [0 (apply max (vals data))]
                      :range [0 width])]

  [:div#bars
   (unify data (fn [[label val]]
                 [:div.bar {:style (str "height: " bar-height ";"
                                        "width: " (s val) ";"
                                        "background-color: gray;")}
                  [:span {:style "color: white;"} label]]))])
