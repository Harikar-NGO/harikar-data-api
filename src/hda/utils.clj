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

(comment
  (verify-password
   "passdo"
   "bcrypt+sha512$999dbfa4cec0f63d9c8ee08ea92e0949$12$a54f7f0bd7f16fa6a66f61202bd90d6917fd4df3f53b3e2e"))
