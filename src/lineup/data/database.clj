(ns lineup.data.database
  (:require [clojure.java.jdbc :refer :all]))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})

(defn get-db-players
  "Get players from DB for the given week. Ignores players without a valid salary."
  [week]
  (query db ["select * from weekly_rankings where week = ? and salary > 0" week]))

(comment

  (query db "select * from weekly_rankings where name = 'Andrew Luck'")
  (get-db-players 3 :ppr)
  )