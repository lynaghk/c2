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

                 ;;required for c2, inline here until c2 jar is cut.
                 [org.clojure/core.match "0.2.0-alpha9"]
                 ]
  
  ;;Include C2 on classpath
  :extra-classpath-dirs ["../src/clj" "../src/cljs"]
  
  :main vrepl.main)
