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


(defn get-matchups
  "Return games which match the given parameters"
  [week start-time end-time]
  (let [sql (cond
              (and start-time end-time)
              ["select * from schedule where week = ? and time >= ? and time <= ?"
               week start-time end-time]

              start-time
              ["select * from schedule where week = ? and time >= ?" week start-time]

              end-time
              ["select * from schedule where week = ? and time <= ?" week end-time]

              :else
              ["select * from schedule where week = ?" week])]
    (query db sql)))

#_(def cached-teams
    "Cache the teams database table"
    (query db ["select * from teams"]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Populate database tables.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn save-season-schedule
  "Saves the schedule to the database"
  [schedule]
  (for [game schedule]
    (insert!
      db :schedule {:week (:gameWeek game)
                    :home (:homeTeam game)
                    :away (:awayTeam game)
                    :date (:gameDate game)
                    :time (:gameTimeET game)
                    :tv (:tvStation game)
                    :winner (:winner game)})))

(defn save-teams
  "Populates the teams table with all of the teams."
  [teams]
  (for [team teams]
    (insert!
      db :teams {:full_name (:fullName team)
                 :short_name (:shortName team)
                 :abbreviation (:code team)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Create database tables. TODO make migrations
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-schedule-table
  "Creates the schedule table in the database"
  []
  (db-do-commands db
                  (create-table-ddl :schedule
                                    [:week :int]
                                    [:home "varchar(3)"]
                                    [:away "varchar(3)"]
                                    [:date :date]
                                    [:time :time]
                                    [:tv "varchar(32)"]
                                    [:winner "varchar(3)"])))

(defn create-teams-table
  "Creates the teams table in the database."
  []
  (db-do-commands db
                  (create-table-ddl
                    :teams
                    [:abbreviation "varchar(3)" "PRIMARY KEY"]
                    [:full_name "varchar(64)"]
                    [:short_name "varchar(32)"])))

(comment

  (query db "select * from weekly_rankings where name = 'Andrew Luck'")
  (get-db-players 3 :ppr)

  )
