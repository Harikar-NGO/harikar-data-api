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
   :port (env :PORT),
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

(defn create-user!
  [{:keys [username email phone-number password]}]
  (let [hashed-password (encrypt-password
                         password)]
    (-> (h/insert-into :users)
        (h/values [{:id (uuid),
                    :username username,
                    :email email,
                    :phone_number phone-number,
                    :password hashed-password}])
        ;; :createdAt :now,
        ;; :updatedAt :now}])
        sql/format
        db-execute-one!)))

(defn create-role!
  [{:keys [role]}]
  (-> (h/insert-into :roles)
      (h/values [{:id (uuid), :role role}])
      sql/format
      db-execute-one!))

(defn add-role!
  [{:keys [user-id role]}]
  (let [role-object (-> (h/select :id)
                        (h/from :roles)
                        (h/where [:= :role role])
                        sql/format
                        db-execute-one!)
        role-id (:id role-object)]
    (-> (h/insert-into :user_in_roles)
        (h/values [{:id (uuid),
                    :user_id user-id,
                    :role_id role-id}])
        sql/format
        db-execute-one!)))

(defn get-all-users
  []
  (-> (h/select :id :username :email)
      (h/from :users)
      (sql/format)
      (db-execute!)))

(defn get-all-roles
  []
  (-> (h/select :id :role)
      (h/from :roles)
      (sql/format)
      (db-execute!)))

(defn get-user-roles-ids
  [user-id]
  (-> (h/select :role_id)
      (h/from :user_in_roles)
      (h/where [:= :user_id user-id])
      sql/format
      db-execute!
      (->> (map #(:role_id %)))))

(defn get-role-name
  [role-id]
  (-> (h/select :role)
      (h/from :roles)
      (h/where [:= :id role-id])
      sql/format
      db-execute-one!
      :role))

(defn get-user-roles
  [user-id]
  (let [roles-ids (get-user-roles-ids user-id)
        role-names (map #(get-role-name %)
                        roles-ids)]
    (vec role-names)))

(defn get-user-by-credentials
  [{:keys [email password]}]
  (let [user (-> (h/select :id
                           :username :email
                           :phone_number
                           :password)
                 (h/from :users)
                 (h/where := :email email)
                 sql/format
                 db-execute-one!)
        user-roles (get-user-roles (:id user))
        user-and-roles
        (assoc user :roles user-roles)
        sanitized-user (dissoc user-and-roles
                               :password)]
    (if (and user
             (:valid (verify-password password
                                      (:password
                                       user))))
      sanitized-user
      nil)))


(comment
  (get-all-users)
  (get-user-by-credentials {:email
                            "ronnie@harikar.org",
                            :password "5GR693NK"})
  (create-role! "admin")
  (add-role!
   {:user-id
    "98cf888f-9498-40fd-b8b8-0c8be9b6c0fa",
    :role "admin"})
  (get-user-roles
   "98cf888f-9498-40fd-b8b8-0c8be9b6c0fa")
  (-> (h/select :*)
      (h/from :user_in_roles)
      sql/format
      db-execute!)
  (j/execute!
   mysql-db
   ["create table users (id varchar(36) primary key,
               username varchar(50),
               email varchar(120),
               phone_number varchar(20),
               password varchar(200),
               createdAt timestamp,
               updatedAt timestamp)"])
  (j/execute!
   mysql-db
   ["create table roles (id varchar(36) primary key,
               role varchar(30))"])
  (j/execute!
   mysql-db
   ["create table user_in_roles (id varchar(36) primary key,
               user_id varchar(36),
               role_id varchar(36))"])
  (-> (h/drop-table :users)
      sql/format
      db-execute-one!)
  (-> (h/drop-table :roles)
      sql/format
      db-execute-one!)
  (-> (h/drop-table :user_in_roles)
      sql/format
      db-execute-one!)
  (create-user!
   {:username "bee",
    :email "bee@beekeeper.com",
    :phone-number "+964-000-000-0000",
    :password "passdo"}))
