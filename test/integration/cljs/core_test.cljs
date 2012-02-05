(ns c2.core-test
  (:use-macros [helpers :only [p profile]])
  (:use [c2.core :only [unify! children]]))

(defn *print-fn* [x]
  (.log js/console x))

(def xhtml "http://www.w3.org/1999/xhtml")

(defn read-attrs [el]
  (let [attrs (.-attributes el)]
    (into {} (for [i (range (.-length attrs))]
               [(keyword  (.-name (aget attrs i)))
                (.-value (aget attrs i))]))))


(def container (.createElementNS js/document xhtml "div"))
;;Appending to html instead of body here because of PhantomJS page.injectJs() wonky behavior
(.appendChild (.querySelector js/document "html") container)


(profile "Count children"
         (assert (= 0 (count (children container)))))

(let [n 100]
  (profile (str "ENTER single tag with " n " data")
           (unify! container (range n) (fn [d idx] [:span {:x 1} (str d)])))

  (let [children (children container)
        fel       (first children)
        attrs     (read-attrs fel)]

    (assert (= n (count children)))
    (assert (= "span" (.toLowerCase (.-nodeName fel))))
    (assert (= "1" (:x attrs))))

  (profile (str "UPDATE single tag with " n " new data and adding new attribute")
           (unify! container (map #(+ % 1000) (range n)) (fn [d idx] [:span {:x 1 :y 2} (str d)])))

  (let [children (children container)
        fel       (first children)
        attrs     (read-attrs fel)]

    (assert (= n (count children)))
    (assert (= "span" (.toLowerCase (.-nodeName fel))))
    (assert (= "1" (:x attrs)))
    (assert (= "2" (:y attrs))))

  )
