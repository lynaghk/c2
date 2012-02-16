(require '[cljs.closure :as closure])
(use '[clojure.java.shell :only [sh]])

(let [js-base "out/"]
  (sh "mkdir" "-p" js-base)
  (closure/build "test/integration/cljs" {:optimizations :whitespace
                                          :pretty-print true
                                          :externs ["vendor/externs.js"]
                                          :output-to (str js-base "/integration_tests.js") 
                                          :output-dir (str js-base)}))
