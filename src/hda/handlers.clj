(ns hda.handlers
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]
            [hda.db :refer
             [create-user! get-all-users
              get-user-by-credentials create-role!
              get-all-roles]]))

(defn index
  [request]
  {:status 200,
   :body (html [:h1 "This is the index page!"])})

(defn not-found
  [request]
  {:status 200,
   :body (html [:h1 "Page not found!"])})

;; User handlers
(defn register
  [{:keys [parameters session]}]
  (let [data (:body parameters)
        user (create-user! data)]
    {:status 201,
     :body {:success
            (str "user "
                 (:username data)
                 " was created succesfully")}}))

(defn login
  [{:keys [parameters]}]
  (let [data (:body parameters)
        user (get-user-by-credentials data)]
    (if (nil? user)
      {:status 401,
       :body {:error "wrong email or password"}}
      {:status 200, :body {:user user}})))

(defn users
  [request]
  {:status 200, :body {:users (get-all-users)}})

;; role handlers

(defn roles
  [request]
  {:status 200, :body {:roles (get-all-roles)}})

(defn create-role
  [{:keys [parameters]}]
  (let [data (:body parameters)
        role (create-role! data)]
    {:status 201,
     :body {:success
            (str "Role "
                 (:role data)
                 " was created succesfully")}}))
