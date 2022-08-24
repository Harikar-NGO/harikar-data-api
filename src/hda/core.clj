(ns hda.core
  (:gen-class)
  (:use [org.httpkit.server :only
         [run-server]])
  (:require [clojure.pprint]
            [hda.routes :refer [app]]
            [ring.middleware.reload :refer
             [wrap-reload]]))

(defn server
  [port]
  (run-server #'app
              {:port port, :join? false}))

(def reloadable-server (wrap-reload #'app))

(defn -main
  [& args]
  (run-server reloadable-server
              {:port 3000, :join? true})
  (println
   "server was started on port 3000"))
