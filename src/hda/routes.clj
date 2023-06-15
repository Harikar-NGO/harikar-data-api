(ns hda.routes
  (:require [hda.handlers :as handle]
            [schema.core :as s]
            [hda.auth :refer
             [wrap-jwt-authentication
              wrap-jwt-authorization
              auth-middleware]]))

(def index-route
  ["/" {:name ::index, :get handle/index}])

(def users-routes
  [["/"
    {:name ::users,
     :get {:middleware [wrap-jwt-authentication
                        wrap-jwt-authorization],
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
            :handler handle/login}}]
   ["/add-roles"
    {:name ::add-roles,
     :post {:middleware [wrap-jwt-authentication
                         auth-middleware],
            :parameters {:body {:user-id s/Str,
                                :role s/Str}},
            :handler handle/add-role}}]
   ["/test"
    {:name ::test,
     :get {:middleware [wrap-jwt-authentication
                        auth-middleware],
           :handler handle/test-route}}]])

(def role-routes
  [["/"
    {:name ::roles,
     :get {:middleware [wrap-jwt-authentication
                        auth-middleware],
           :handler handle/roles}}]
   ["/create"
    {:name ::create-role,
     :post {:middleware [wrap-jwt-authentication
                         auth-middleware],
            :parameters {:body {:role s/Str}},
            :handler handle/create-role}}]])
