(defproject c2 "0.0.1-SNAPSHOT"
  :description "Declarative data visualization in Clojure(Script)."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [com.google.javascript/closure-compiler "r1592"]
                 
                 
                 [goog-jar "1.0.0"] ;;Required for C2 because of its Pinot submodule dependency; will drop when new Pinot JAR is cut or C2 stops using Pinot all together.
                 [clojure.core.match.js :only [match]]
                 [clj-iterate "0.95-SNAPSHOT"]]
  
  :dev-dependencies [[midje "1.3.1"]])
