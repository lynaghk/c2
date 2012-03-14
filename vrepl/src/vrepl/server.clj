(ns vrepl.server
  (:use compojure.core
        [ring.middleware.file :only [wrap-file]]
        [aleph.http :only [start-http-server
                           wrap-ring-handler wrap-aleph-handler]]
        [lamina.core :only [enqueue permanent-channel receive siphon]]
        [hiccup.core :only [html]]
        [clojure.data.json :only [read-json json-str]])
  (:require [vrepl.core :as core]))

;;Messages to livereload client.
;;http://help.livereload.com/kb/ecosystem/livereload-protocol
(defn lr-reload [path-to-reload]
  {:command "reload"
   :path path-to-reload
   :liveCSS true})

(defn lr-alert [msg]
  {:command "alert"
   :message msg})

(defn lr-hello []
  {:command "hello"
   :protocols ["http://livereload.com/protocols/official-7"]
   :serverName "vrepl"})



(def broadcast-channel (permanent-channel))

;;Whenever the content of the current page changes, broadcast to livereload
(add-watch core/current-page :reload
           (fn [_ _ _ _] (enqueue broadcast-channel (json-str (lr-reload "/")))))

(defn livereload-handler
  "When a websocket client connects, this function runs."
  [ch handshake]
  (receive ch (fn [msg]
                (when (= "hello" (:command (read-json msg)))
                  (println "Client connected.")
                  ;;Say hello back
                  (enqueue ch (json-str (lr-hello)))
                  ;;Subscribe it to events
                  (siphon broadcast-channel ch)))))

(defroutes main-routes
  (GET "/" []
       (html [:html
                     [:head
                      [:script {:src (str "/livereload.js?port=" (:port @core/opts))}]
                      [:style "body { background-color: #222222; color: white;}"]]
              [:body

                      @core/current-page]]))
  (GET "/livereload" []
       (wrap-aleph-handler livereload-handler)))

(def app (-> main-routes
             (wrap-file "public")
             (wrap-ring-handler)))

(defn start-server! []
  (start-http-server app {:port (:port @core/opts) :websocket true}))
