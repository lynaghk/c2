(ns c2.layout
  (:refer-clojure :exclude [+ - =])
  (:use-macros [c2.util :only [p half]])
  (:use [c2.core :only [translate children select select-all]]
        [cassowary.core :only [+ - = cvar constrain! stay! simplex-solver]])
  (:require [pinot.dom :as dom]))

(defn distribute! [selector]
  (let [solver (simplex-solver)
        spacing  (cvar 0)
        nodes  (dom/query selector)
        container (dom/parent (first nodes))
        container-height (js/parseFloat (dom/attr container :height))
        positions    (map #(let [base {:top (cvar (dom/attr % :top))
                                       :left (cvar (dom/attr % :left))
                                       :height (cvar (dom/attr % :height))
                                       :width (cvar (dom/attr % :width))}
                                 bottom (cvar)
                                 right (cvar)]

                             (doto solver
                               (stay! (:height base))
                               (stay! (:width base))
                               (constrain! (= bottom (+ (:top base) (:height base))))
                               (constrain! (= right (+ (:left base) (:width base)))))
                             (merge base {:bottom bottom, :right right}))
                          nodes)]

    ;;Constraints to distribute the elements across the entire height
    (constrain! solver (= 0 (:top (first positions))))
    (constrain! solver (= container-height
                          (:bottom (last positions))))

    (doseq [[t b] (partition 2 1 positions)]
      (constrain! solver (= (:top b) (+ (:bottom t) spacing))))

    (doseq [[node pos] (map vector nodes positions)]
      (dom/attr node (translate (.value (:left pos))
                                (.value (:top pos)))))))

(defn grid [n ncol container-width]
  (let [sep-width (/ container-width (inc ncol))]
    {:positions  (map (fn [idx]
                        {:top (* sep-width (Math/floor (/ idx ncol)))
                         :left (* sep-width (mod idx ncol))})
                      (range n))
     :sep-width sep-width
     :container-width container-width
     :container-height  (* sep-width (inc (Math/floor (/ n ncol))))}))

(defn grid!
  "Positions objects in parent container to form a grid ncol items wide"
  [container ncol & {:keys [selector]}]
  (when-let [container (select container)]
    (let [nodes (if selector
                  (select-all selector)
                  (children container))
          container-width  (js/parseFloat (dom/css container :width))
          sep-width (/ container-width (inc ncol))
          {:keys [positions container-height]}  (grid (count nodes) ncol container-width)]

      ;;Position the nodes
      (doseq [[{:keys [top left]} node] (map vector positions nodes)]
        (dom/css node {:-webkit-transform (str "translate(" left "px" ", " top "px" ")")}))

      ;;Manually set container height according to the number of rows
      (dom/css container :height (str container-height "px")))))
