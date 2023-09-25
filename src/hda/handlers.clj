(ns hda.handlers
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]
            [hda.db :refer
             [create-user! get-all-users
              get-user-by-credentials create-role!
              get-all-roles email-taken?]]))

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
  [{:keys [parameters]}]
  (let [data (:body parameters)]
    (if (email-taken? (:email data))
      {:status 401,
       :body {:error "This email is taken"}}
      (do (create-user! data)
          {:status 201,
           :body
           {:success
            (str "user "
                 (:username data)
                 " was created succesfully")}}))))

(defn login
  [{:keys [parameters]}]
  (let [data (:body parameters)
        user (get-user-by-credentials data)]
    (if (nil? user)
      {:status 401,
       :body {:error "wrong email or password"}}
      {:status 200,
       :session (select-keys (into {} user)
                             [:username :email
                              :role]),
       :body {:user user}})))

(defn info
  [{:keys [session]}]
  (if (seq session)
    {:status 200, :body session}
    {:status 401,
     :body {:error "You are not signed in"}}))

(defn logout
  [request]
  {:status 200, :session nil})

(defn users
  [{:keys [session]}]
  (if (seq session)
    (if (= (:role session) "admin")
      {:status 200,
       :body {:users (get-all-users)}}
      {:status 401,
       :body {:error "You are not an admin"}})
    {:status 401,
     :body {:error "Sign in first"}}))

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
