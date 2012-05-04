(ns vrepl.main
  (:gen-class)
  (:use [clojure.tools.cli :only [cli]])
  (:require [vrepl.core :as core]
            [vrepl.server :as server]))

(defn extract-assets! [path]
  (println  "Extracting assets to" path)
  
  (when (not (.exists (java.io.File. path)))
    (.mkdirs (java.io.File. path)))
  
  ;;Hardcoded list of sample files in the JAR to extract.
  ;;Pull request if you want to write something more general.
  (doseq [f ["boxplot.clj" "choropleth.clj"]]
    (spit (str path "/" f) (slurp (ClassLoader/getSystemResource (str "samples/" f))))))


(defn monitor-and-start [path port]
  (println "Monitoring:" path)
  (core/monitor-files! path)
  (server/start-server!)
  (println "Server started on port" port))

(defn -main [& args]
  (let [[{:keys [path port] :as opts} args banner] (cli args
                                                        ["-h" "--help" "Show help" :default false :flag true]
                                                        ["--compile-all" "Compile all files to HTML" :default false :flag true]
                                                        ["--extract" "Extract built-in samples directory before starting watcher" :default false :flag true]
                                                        ["--path" "Path to watch (recursive)"
                                                         :default (str (System/getProperty "user.dir") "/" "samples")
                                                         :parse-fn #(str (System/getProperty "user.dir") "/" %)]
                                                        ["--port" "Webserver port" :default 8987 :parse-fn #(Integer. %)])]

    (reset! core/opts opts)

    (when (:help opts)
      (println banner)
      (System/exit 0))

    (when (:compile-all opts) 
      (core/compile-all! path)
      (System/exit 0))
    
    (if (:extract opts)
      (extract-assets! path)
      (println "Run with --extract flag to extract sample files."))

    (when (not (.exists (java.io.File. path)))
      (println "Path" (str "\"" path "\"") "not found")
      (System/exit 1))

    (reset! core/current-page [:div [:h1 "Visual REPL ready."]
                               [:span "Monitoring: " path]])
    
    (monitor-and-start path port)))
