(ns hda.routes
  (:require [hda.handlers :as handle]
            [schema.core :as s]))

(def index-route
  ["/" {:name ::index, :get handle/index}])

(def users-routes
  ["/users" {:name ::users}
   ["/" {:get {:handler handle/users}}]
   ["/register"
    {:name ::register,
     :post {:parameters {:body {:username s/Str,
                                :password s/Str,
                                :email s/Str,
                                :role s/Str}},
            :handler handle/register}}]
   ["/login"
    {:name ::login,
     :post {:parameters {:body {:email s/Str,
                                :password s/Str}},
            :handler handle/login}}]
   ["/info"
    {:name ::info, :get {:handler handle/info}}]
   ["/logout"
    {:name ::logout,
     :post {:handler handle/logout}}]])

(def role-routes
  ["/roles" {:name ::roles}
   ["/" {:get {:handler handle/roles}}]
   ["/create"
    {:name ::create-role,
     :post {:parameters {:body {:role s/Str}},
            :handler handle/create-role}}]])

(def api-routes
  ["/api" {:name ::api} users-routes role-routes])
