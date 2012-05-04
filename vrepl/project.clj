(defproject vrepl "1.1.0"
  :description "Standalone C2 Visual REPL / webserver."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/data.json "0.1.2"]
                 [org.clojure/tools.cli "0.2.1"]
                 [org.apache.commons/commons-vfs2 "2.0"]
                 [compojure "1.0.1"]
                 [hiccup "1.0.0"]
                 [aleph "0.2.1-beta2"]

                 [com.keminglabs/vomnibus "0.1.0"]
                 [com.keminglabs/c2 "0.1.0-beta2"]]
  :min-lein-version "2.0.0"
  :main vrepl.main)
