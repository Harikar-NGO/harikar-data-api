(ns hda.env
  (:require [clojure.edn :as edn]))

(def envvars (edn/read-string (slurp "env.edn")))

(defn env
  [k]
  (or (k envvars) (System/getenv (name k))))
