(ns hda.utils
  (:require [buddy.hashers :as hashers]))

(defn uuid
  []
  (.toString (java.util.UUID/randomUUID)))

(defn encrypt-password
  [password]
  (hashers/derive password {:alg :bcrypt+sha512}))

(defn verify-password
  [password hashed-password]
  (hashers/verify password hashed-password))
