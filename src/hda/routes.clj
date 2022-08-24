(ns hda.routes
  (:require [compojure.core :refer
             [GET POST defroutes context]]
            [compojure.route :as
             compojure-route]
            [hda.handlers :as handle]))

(defroutes
 app
 (GET "/" [] handle/index)
 (context
  "/users"
  []
  (GET "/" [] handle/users)
  (GET "/:id" [id] (handle/user id))
  (POST "/register" [] handle/register))
 (compojure-route/not-found
  handle/not-found))
