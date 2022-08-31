(ns hda.core
  (:gen-class)
  (:require
   [clojure.pprint]
   [ring.middleware.reload :refer [wrap-reload]]
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
   [hda.routes :refer [index-route users-routes]]
   [org.httpkit.server :refer [run-server]]))

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

(defn server
  [port]
  (run-server #'app {:port port, :join? false}))

(def reloadable-server (wrap-reload #'app))

(defn -main
  [& args]
  (run-server reloadable-server
              {:port 3000, :join? true})
  (println "server was started on port 3000"))
