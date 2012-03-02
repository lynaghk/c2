(ns vrepl.core
  (:gen-class)
  (:import org.apache.commons.vfs2.VFS
           org.apache.commons.vfs2.impl.DefaultFileMonitor
           org.apache.commons.vfs2.FileListener))

(def opts (atom {}))
(def current-page (atom [:p (str "Visual REPL ready.")]))

(defn output-clj!
  "Output result of freshly reloaded Clojure file"
  [res]
  (reset! current-page res))


(defn reload! [vfs-filename]
  (let [path (.getPath vfs-filename)]
    (println "Reloading:" path)
    (case (.getExtension vfs-filename)
      "clj"  (output-clj! (load-file path))
      "cljs" "CLJS not implemented yet"
      (println "I have no idea what to do with" path))))

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
