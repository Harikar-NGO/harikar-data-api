(ns hda.handlers
  (:require [hiccup.core :refer [html]]
            [hda.db :refer
             [get-all-users create-user]]))

(defn index
  [request]
  {:status 200,
   :body (html [:h1
                "This is the index page!"])})

(defn not-found
  [request]
  (html [:div
         [:h1 "The page is not found"]]))

(defn register
  [{:keys [parameters]}]
  (let [data (:body parameters)
        user (create-user data)]
    {:status 201, :body {:user user}}))

(defn users [request] (get-all-users))

(defn user
  [id]
  (html [:h1 "Hello from " id]))
