(ns hda.core
  (:gen-class)
  (:require
    [clojure.pprint]
    [ring.middleware.reload :refer [wrap-reload]]
    [ring.middleware.session :refer
     [wrap-session]]
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
    [jdbc-ring-session.core :refer [jdbc-store]]
    [hda.routes :refer [index-route api-routes]]
    [hda.handlers :as handle]
[hda.db :refer [mysql-db]]))

(def app
  (ring/ring-handler
   (ring/router
    [index-route api-routes]
    {:data {:coercion
            reitit.coercion.schema/coercion,
            :muuntaja m/instance,
            :middleware
            [[wrap-session
              {:cookie-attrs {:secure true,
                              :max-age 30},
               :store (jdbc-store mysql-db)}]
             format-negotiate-middleware
             format-response-middleware
             exception-middleware
             format-request-middleware
             coerce-exceptions-middleware
             coerce-request-middleware
             coerce-response-middleware]}})
   (ring/routes
    (ring/redirect-trailing-slash-handler)
    (ring/create-default-handler
     {:not-found handle/not-found}))))

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
  (stop-server)
  )
