(ns vrepl.main
  (:gen-class)
  (:use [clojure.tools.cli :only [cli]])
  (:require [vrepl.core :as core]
            [vrepl.server :as server]))

(defn extract-assets! [path]
  (println  "Extracting assets to" path)
  
  (when (not (.exists (java.io.File. path)))
    (.mkdir (java.io.File. path)))
  
  ;;Hardcoded list of sample files in the JAR to extract.
  ;;Pull request if you want to write something more general.
  (doseq [f ["boxplot.clj" "choropleth.clj"]]
    (spit (str path "/" f) (slurp (ClassLoader/getSystemResource f)))))


(defn monitor-and-start [path port]
  (println "Monitoring:" path)
  (core/monitor-files! path)
  (server/start-server!)
  (println "Server started on port" port))

(defn -main [& args]
  (let [[{:keys [path port] :as opts} args banner] (cli args
                                                        ["-h" "--help" "Show help" :default false :flag true]
                                                        ["--compile-all" "Compile all files to HTML" :default false :flag true]
                                                        ["--extract-to" "Extract built-in samples to relative directory and watch 'em"
                                                         :parse-fn #(str (System/getProperty "user.dir") "/" %)]
                                                        ["--path" "Path to watch (recursive)" :default (str (System/getProperty "user.dir") "/samples")]
                                                        ["--port" "Webserver port" :default 8987 :parse-fn #(Integer. %)])]

    (reset! core/opts opts)

    (when (:help opts)
      (println banner)
      (System/exit 0))


    (cond
     (:extract-to opts) (do
                          (extract-assets! (:extract-to opts))
                          (monitor-and-start (:extract-to opts) port))

     (:compile-all opts) (do
                           (core/compile-all! path)
                           (System/exit 0))

     :else (monitor-and-start path port))))


#_(when (not (.exists (java.io.File. path)))
    (println "Path\"" path "\"not found")
    (System/exit 1))
