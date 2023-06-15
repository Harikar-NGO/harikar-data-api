(ns hda.handlers
  (:require
   [clojure.string :as str]
   [hiccup.core :refer [html]]
   [hda.db :refer
    [create-user! get-all-users
     get-user-by-credentials get-all-roles
     create-role! add-role!]]
   [hda.auth :refer [create-token read-token]]))

(defn index
  [request]
  {:status 200,
   :body (html [:h1 "This is the index page!"])})

;; User handlers
(defn register
  [{:keys [parameters]}]
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
      {:status 200,
       :body {:user user,
              :token (create-token user)},
       :cookies {"Token" {:value (create-token
                                  user),
                          :path "/api"}}})))

(defn add-role
  [{:keys [parameters]}]
  (let [data (:body parameters)
        id (:user-id data)
        role (add-role! data)]
    {:status 201,
     :body {:success
            (str "Role "
                 (:role data)
                 " was added to the user")}}))

(defn users
  [request]
  {:status 200, :body {:users (get-all-users)}})

(defn test-route
  [{:keys [parameters]}]
  (let [data (:body parameters)
        user (get-user-by-credentials data)]
    (if (nil? user)
      {:status 401,
       :body {:error "wrong email or password"}}
      {:status 200, :body {:user user}})))

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
