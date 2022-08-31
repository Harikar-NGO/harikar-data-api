(ns hda.routes
  (:require [hda.handlers :as handle]
            [schema.core :as s]))

(def index-route
  ["/" {:name ::index, :get handle/index}])

(def users-routes
  [["/"
    {:name ::users, :get {:handler handle/users}}]
   ["/register"
    {:name ::register,
     :post {:parameters {:body {:username s/Str,
                                :password s/Str,
                                :email s/Str}},
            :handler handle/register}}]
   ["/login"
    {:name ::user,
     :get {:parameters {:body {:email s/Str,
                               :password s/Str}}},
     :handler handle/login}]])
