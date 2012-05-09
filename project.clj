(defproject com.keminglabs/c2 "0.1.0-RC3"
  :description "Declarative data visualization in Clojure(Script)."
  :url "http://keminglabs.com/c2/"
  :license {:name "BSD" :url "http://www.opensource.org/licenses/BSD-3-Clause"}
  
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/core.match "0.2.0-alpha9"]
                 [clj-iterate "0.96"]]

  :profiles {:dev {:dependencies [[midje "1.3.1"]
                                  [lein-midje "1.0.8"]
                                  [com.stuartsierra/lazytest "1.2.3"]

                                  [com.keminglabs/vomnibus "0.3.0"]]
                   ;;Required for lazytest.
                   :repositories {"stuartsierra-releases" "http://stuartsierra.com/maven2"
                                  "stuartsierra-snapshots" "http://stuartsierra.com/m2snapshots"}}}
  
  :min-lein-version "2.0.0"
  
  :plugins [[com.keminglabs/cljx "0.1.2"]
            [lein-cljsbuild "0.1.8"]
            [lein-midje "2.0.0-SNAPSHOT"]
            [lein-marginalia "0.7.0"]]

  :source-paths ["src/clj" "src/cljs"
                 ;;See src/cljx/README.markdown
                 ".generated/clj" ".generated/cljs"

                 ;;Uncomment & change accordingly if you want to build/test with a different version of ClojureScript besides what comes with cljsbuild.
                 ;;For details: https://github.com/emezeske/lein-cljsbuild/issues/58
                 ;;"../software/clojurescript/src/clj" "../software/clojurescript/src/cljs"

                 ;;Use a marginalia fork that documents .cljx files
                 ;;"../software/marginalia/src"
                 ]

  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path ".generated/clj"
                   :rules cljx.rules/clj-rules}

                  {:source-paths ["src/cljx"]
                   :output-path ".generated/cljs"
                   :extension "cljs"
                   :rules cljx.rules/cljs-rules}]}
  
  :cljsbuild {:builds {:test {:source-path "test/integration/cljs"
                              :compiler {:output-to "out/test/integration.js"
                                         :optimizations :simple
                                         :pretty-print true}}}
              :test-commands {"integration" ["phantomjs"
                                             "test/integration/runner.coffee"]}}


  ;;generate cljx before JAR
  :hooks [cljx.hooks])
