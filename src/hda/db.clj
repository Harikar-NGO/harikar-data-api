(ns hda.db
  (:gen-class)
  (:require [next.jdbc :as j]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [hda.env :refer [env]]
            [hda.utils :refer
             [uuid encrypt-password
              verify-password]]))

(def mysql-db
  {:host (env :HOST),
   :dbtype "mysql",
   :dbname (env :DBNAME),
   :user (env :USER),
   :password (env :PASSWORD)})

(defn db-execute!
  [q]
  (j/execute! mysql-db
              q
              {:return-keys true,
               :builder-fn
               rs/as-unqualified-maps}))

(defn db-execute-one!
  [q]
  (j/execute-one! mysql-db
                  q
                  {:return-keys true,
                   :builder-fn
                   rs/as-unqualified-maps}))

(defn create-role!
  [{:keys [role]}]
  (-> (h/insert-into :roles)
      (h/values [{:id (uuid), :role role}])
      sql/format
      db-execute-one!))

(defn get-all-roles
  []
  (-> (h/select :id :role)
      (h/from :roles)
      (sql/format)
      (db-execute!)))

(defn get-role
  [role]
  (-> (h/select :id)
      (h/from :roles)
      (h/where := :role role)
      (sql/format)
      (db-execute-one!)
      :id))

(defn create-user!
  [{:keys [username email password role]}]
  (let [hashed-password (encrypt-password
                         password)]
    (-> (h/insert-into :users)
        (h/values [{:id (uuid),
                    :username username,
                    :email email,
                    :password hashed-password,
                    :role_id (get-role role)}])
        sql/format
        db-execute-one!)))

(defn get-all-users
  []
  (-> (h/select :users.id :username :email :role)
      (h/from :users)
      (h/join :roles
              [:= :roles.id :users.role_id])
      (sql/format)
      (db-execute!)))

(defn get-user-by-credentials
  [{:keys [email password]}]
  (let [user (-> (h/select :users.id
                           :username :email
                           :password :role)
                 (h/from :users)
                 (h/where := :email email)
                 (h/join :roles
                         [:= :roles.id
                          :users.role_id])
                 sql/format
                 db-execute-one!)
        sanitized-user (dissoc user :password)]
    (if (and user
             (:valid (verify-password password
                                      (:password
                                       user))))
      sanitized-user
      nil)))


(comment
  (get-all-users)
  (get-user-by-credentials {:email
                            "Davin@harikar.org",
                            :password "davindav"})
  (j/execute!
   mysql-db
   ["create table users (id varchar(36) primary key,
               username varchar(50),
               email varchar(120),
               phone_number varchar(20),
               password varchar(200),
               role_id varchar(36),
               createdAt timestamp,
               updatedAt timestamp)"])
  (j/execute!
   mysql-db
   ["create table roles (id varchar(36) primary key,
               role varchar(30))"])
  (-> (h/drop-table :users)
      sql/format
      db-execute-one!)
  (-> (h/drop-table :roles)
      sql/format
      db-execute-one!)
  (create-user! {:username "bee",
                 :email "bee@beekeeper.com",
                 :phone-number
                 "+964-000-000-0000",
                 :password "passdo"}))
