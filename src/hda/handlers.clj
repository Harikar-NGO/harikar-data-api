(ns hda.handlers
  (:require [hiccup.core :refer [html]]
            [hda.db :refer
             [create-user! get-all-users
              get-user-by-credentials]]))

(defn response
  [status body & {:as headers}]
  {:status status, :body body, :headers headers})

(def ok (partial response 200))
(def created (partial response 201))
(def unauthorized (partial response 401))

(defn index
  [request]
  (response ok
            (html [:h1
                   "This is the index page!"])))

(defn register
  [{:keys [parameters]}]
  (let [data (:body parameters)
        user (create-user! data)]
    (response
     created
     {:success (str
                "user "
                (:username data)
                " was created succesfully")})))

(defn users
  [request]
  (response ok {:users (get-all-users)}))

(defn login
  [{:keys [parameters]}]
  (let [data (:body parameters)
        user (get-user-by-credentials data)]
    (if (nil? user)
      (response unauthorized
                {:error
                 "wrong email or password"})
      (response ok {:user user}))))
