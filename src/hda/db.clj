(ns hda.db
  (:require
   [clojure.java.jdbc :as j]
   [honey.sql :as sql]
   [honey.sql.helpers :refer
    [select select-distinct from join
     left-join right-join where for group-by
     having union order-by limit offset
     values columns update insert-into set
     composite delete delete-from truncate]
    :as h]
   [hda.env :refer [env]]
   [buddy.hashers :as hashers]))

;; https://search.maven.org/artifact/mysql/mysql-connector-java/8.0.30/jar

(defn uuid
  []
  (.toString (java.util.UUID/randomUUID)))

(defn encrypt-password
  [password]
  (hashers/derive password
                  {:alg :bcrypt+sha512}))

(defn verify-password
  [password hashed-password]
  (hashers/verify password hashed-password))

(def mysql-db
  {:host (env :HOST),
   :dbtype "mysql",
   :dbname (env :DBNAME),
   :user (env :USER),
   :password (env :PASSWORD)})

(defn query [q] (j/query mysql-db q))

(defn insert!
  [q]
  (j/db-do-prepared-return-keys mysql-db q))

(defn delete! [q] (j/execute! mysql-db q))

(defn create-user
  [{:keys [username email password]}]
  (let [hashed-password (encrypt-password
                         password)
        created-user
        (-> (h/insert-into :users)
            (h/values
             [{:id (uuid),
               :username username,
               :email email,
               :password hashed-password,
               :createdAt :now,
               :updatedAt :now}])
            (sql/format)
            (insert!)
            (first))
        sanatized-user (dissoc created-user
                               :password)]
    sanatized-user))

(defn get-all-users
  []
  (query (-> (h/select :*)
             (h/from :users)
             (sql/format))))

(defn delete-user-by-id!
  [user-id]
  (delete! (-> (h/delete-from :users)
               (h/where [:= :id user-id])
               (sql/format))))

(defn verify-user-password
  [password user-id]
  (let [hashed-password
        (get
         (first
          (query
           (-> (h/select :password :email)
               (h/from :users)
               (h/where [:= :id user-id])
               (sql/format))))
         :password)]
    (verify-password password
                     hashed-password)))

(comment
  (j/db-do-commands
   mysql-db
   (j/create-table-ddl
    :users
    [[:id "varchar(36)" :primary :key]
     [:username "varchar(50)"]
     [:email "varchar(120)"]
     [:password "varchar(200)"]
     [:createdAt "timestamp"]
     [:updatedAt "timestamp"]]))
  (j/db-do-commands mysql-db
                    (j/drop-table-ddl
                     :users))
  (create-user {:username "ron",
                :email "ron@email.com",
                :password "password"})
  (delete-user-by-id!
   "3ff45457-4391-4aa0-ba1a-32b03d2b8576")
  (verify-user-password
   "password"
   "99469db7-ece6-42f8-b8ec-e28a5c0f311e")
  (verify-password
   "password"
   "pbkdf2+sha256$282b785dae29b08f2dfb7776$100000$ab0ac8da185ba3af87830b332a1ef7734867187175afffc192bd6e3c69710350")
  (query (-> (h/select :*)
             (h/from :users)
             (sql/format)))
  (get
   (first
    (query
     (->
       (h/select :password)
       (h/from :users)
       (h/where
        [:= :id
         "f146654f-67a5-4e10-927a-ca8a0a9343ed"])
       (sql/format))))
   :password))
