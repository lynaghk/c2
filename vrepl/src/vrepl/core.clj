(ns vrepl.core
  (:import org.apache.commons.vfs2.VFS
           org.apache.commons.vfs2.impl.DefaultFileMonitor
           org.apache.commons.vfs2.FileListener)
  (:require [clojure.string :as str])
  (:use [hiccup.core :only [html]]))

(def opts (atom {}))
(def current-page (atom [:div]))

(defn output-clj!
  "Output result of freshly reloaded Clojure file"
  [res]
  (reset! current-page res))


(defn reload! [vfs-filename]
  (when-not (-> vfs-filename
              .getBaseName
              (.startsWith "."))
    (let [path (.getPath vfs-filename)]
      (println "Reloading:" path)
      (case (.getExtension vfs-filename)
        "clj"  (output-clj! (load-file path))
        "cljs" "CLJS not implemented yet"
        (println "I have no idea what to do with" path)))))

(defn monitor-files! [path]
  (let [fm (DefaultFileMonitor. (reify FileListener
                                  (fileChanged [_ e]
                                    (reload! (.getName (.getFile e))))
                                  (fileCreated [_ e])
                                  (fileDeleted [_ e])))]

    (.addFile fm (.resolveFile (VFS/getManager)
                               path))
    (.setRecursive fm true)
    (.start fm)))


(defn compile-all! [path]
  (doseq [f (.listFiles (java.io.File. path))
          :when (re-matches #".*\.clj" (.getName f))]
    (spit (str/replace (.getPath f) "clj" "html")
          (html (load-file (.getPath f))))))


