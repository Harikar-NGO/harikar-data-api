(ns hda.routes
  (:require [hda.handlers :as handle]
            [schema.core :as s]
            [hda.auth :refer
             [wrap-jwt-authentication
              auth-middleware]]))

(def index-route
  ["/" {:name ::index, :get handle/index}])

(def users-routes
  [["/"
    {:name ::users,
     :get {:middleware [wrap-jwt-authentication
                        auth-middleware],
           :handler handle/users}}]
   ["/register"
    {:name ::register,
     :post {:parameters {:body {:username s/Str,
                                :password s/Str,
                                :email s/Str}},
            :handler handle/register}}]
   ["/login"
    {:name ::login,
     :post {:parameters {:body {:email s/Str,
                                :password s/Str}},
            :handler handle/login}}]])
