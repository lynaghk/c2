(require '[cljs.closure :as closure])
(use '[clojure.java.shell :only [sh]])

(let [js-base "resources/public/js"]
  (sh "mkdir" "-p" js-base)
  (closure/build "src/cljs" {:optimizations :whitespace
                             :externs ["vendor/externs.js"]
                             :output-to (str js-base "/main.js") 
                             :output-dir (str js-base "/out")}))
