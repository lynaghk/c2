(defproject com.keminglabs/c2 "0.0.1-SNAPSHOT"
  :description "Declarative data visualization in Clojure(Script)."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/core.match "0.2.0-alpha9"]
                 [clj-iterate "0.95-SNAPSHOT"]]
  
  :dev-dependencies [[midje "1.3.1"]
                     [lein-midje "1.0.8"]
                     [com.stuartsierra/lazytest "1.2.3"]]
  
  ;;Required for lazytest.
  :repositories {"stuartsierra-releases" "http://stuartsierra.com/maven2"
                 "stuartsierra-snapshots" "http://stuartsierra.com/m2snapshots"}
  
  :source-paths ["src/clj"])
