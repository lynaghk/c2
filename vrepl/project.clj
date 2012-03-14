(defproject vrepl "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/data.json "0.1.2"]
                 [org.clojure/tools.cli "0.2.1"]
                 [org.apache.commons/commons-vfs2 "2.0"]
                 [compojure "1.0.1"]
                 [hiccup "0.3.8"]
                 [aleph "0.2.1-alpha2-SNAPSHOT"]

                 [com.keminglabs/vomnibus "0.0.1-SNAPSHOT"]
                 [com.keminglabs/c2 "0.0.1-SNAPSHOT"]]
  
  :main vrepl.main)
