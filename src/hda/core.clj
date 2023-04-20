(ns hda.core
  (:gen-class)
  (:require
   [clojure.pprint]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.adapter.jetty :refer [run-jetty]]
   [muuntaja.core :as m]
   [reitit.coercion.schema]
   [reitit.ring :as ring]
   [reitit.ring.coercion :refer
    [coerce-exceptions-middleware
     coerce-request-middleware
     coerce-response-middleware]]
   [reitit.ring.middleware.exception :refer
    [exception-middleware]]
   [reitit.ring.middleware.muuntaja :refer
    [format-request-middleware
     format-response-middleware
     format-negotiate-middleware]]
   [hda.routes :refer
    [index-route users-routes]]))

(def app
  (ring/ring-handler
   (ring/router
    [["/api" index-route ["/users" users-routes]]]
    {:data
     {:coercion reitit.coercion.schema/coercion,
      :muuntaja m/instance,
      :middleware [format-negotiate-middleware
                   format-response-middleware
                   exception-middleware
                   format-request-middleware
                   coerce-exceptions-middleware
                   coerce-request-middleware
                   coerce-response-middleware]}})
   (ring/routes
    (ring/redirect-trailing-slash-handler)
    (ring/create-default-handler
     {:not-found
      (constantly
       {:status 404,
        :body {:error "Route not found"}})}))))

(defonce server (atom nil))

(def reloadable-server (wrap-reload #'app))

(defn start-server
  []
  (reset! server (run-jetty #'reloadable-server
                            {:port 3000,
                             :join? false})))

(defn stop-server
  []
  (when-some [s @server]
    (.stop s)
    (reset! server nil)))

(defn -main
  [& args]
  (run-jetty #'app {:port 3000, :join? true})
  (println "server was started on port 3000"))

(comment
  (start-server)
  (stop-server))
