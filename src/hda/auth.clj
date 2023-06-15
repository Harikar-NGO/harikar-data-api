(ns hda.auth
  (:require [buddy.auth :refer
             [authenticated? throw-unauthorized]]
            [buddy.auth.middleware :refer
             [wrap-authentication
              wrap-authorization]]
            [buddy.auth.protocols :as proto]
            [buddy.sign.jwt :as jwt]
            [hda.env :refer [env]]))

(defn- handle-unauthorized-default
  "A default response constructor for an unauthorized request."
  [request]
  (if (authenticated? request)
    {:status 403,
     :headers {},
     :body "Permission denied"}
    {:status 401,
     :headers {},
     :body "Unauthorized"}))


(defn- parse-cookie
  [request]
  (some->> (((request :cookies) "Token") :value)))

(defn jws-cookie-backend
  [{:keys [secret authfn unauthorized-handler
           options on-error],
    :or {authfn identity}}]
  {:pre [(ifn? authfn)]}
  (reify
   proto/IAuthentication
     (-parse [_ request] (parse-cookie request))
     (-authenticate [_ request data]
       (try
         (authfn (jwt/unsign data secret options))
         (catch clojure.lang.ExceptionInfo e
           (let [data (ex-data e)]
             (when (fn? on-error)
               (on-error request e))
             nil))))
   proto/IAuthorization
     (-handle-unauthorized [_ request metadata]
       (if unauthorized-handler
         (unauthorized-handler request metadata)
         (handle-unauthorized-default request)))))

(def jwt-secret (env :JWTSECRET))
(def backend
  (jws-cookie-backend {:secret jwt-secret}))

(defn wrap-jwt-authentication
  [handler]
  (wrap-authentication handler backend))

(defn wrap-jwt-authorization
  [handler]
  (wrap-authorization handler backend))

(defn auth-middleware
  [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      {:status 401, :body "unauthorized"})))

(defn create-token
  [payload]
  (jwt/sign payload jwt-secret))

(defn read-token
  [payload]
  (jwt/unsign payload jwt-secret))
