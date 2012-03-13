(ns vrepl.main
  (:gen-class)
  (:use [clojure.tools.cli :only [cli]])
  (:require [vrepl.core :as core]
            [vrepl.server :as server]))

(defn -main [& args]
  (let [[opts args banner] (cli args
                                ["-h" "--help" "Show help" :default false :flag true]
                                ["--compile-all" "Compile all files to HTML" :default false :flag true]
                                ["--path" "Path to watch (recursive)" :default (str (System/getProperty "user.dir") "/samples")]
                                ["--port" "Webserver port" :default 8987 :parse-fn #(Integer. %)])]

    (reset! core/opts opts)
    
    (when (:help opts)
      (println banner)
      (System/exit 0))
    
    (when (not (.exists (java.io.File. (:path opts))))
      (println "Path\"" (:path opts) "\"not found")
      (System/exit 1))

    (when (:compile-all opts)
      (core/compile-all! (:path opts))
      (System/exit 0))
    
    
    (println "Monitoring:" (:path opts))

    (core/monitor-files! (:path opts))
    (server/start-server!)
    (println "Server started on port" (:port opts))))
