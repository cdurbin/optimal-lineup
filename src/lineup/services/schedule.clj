(ns lineup.services.schedule
  "Functions related to the NFL schedule."
  (:require [lineup.data.ffn :as ffn]
            [lineup.data.database :as db]
            [clojure.string :as str]))

(defn get-matchups-by-schedule
  "Return the teams by their start and end time."
  ([week]
   (get-matchups-by-schedule week nil nil))
  ([week start-time]
   (get-matchups-by-schedule week start-time nil))
  ([week start-time end-time]
   (db/get-matchups week start-time end-time)))

(defn get-all-matchups
  [week]
  "Returns all matchups for the week"
  (get-matchups-by-schedule week))

#_(defn get-1pm-matchups
    [week]
    "Returns all 1:00 matchups for the week."
    (get-matchups-by-schedule week start-time end-time))

(defn get-sunday-for-week
  [week]
  "Figures out what the Sunday date is for the provided week."
  (->> (map :time (get-matchups-by-schedule week nil nil))
       (map #(str/replace % #"T.*" ""))
       frequencies
       (sort-by val)
       reverse
       first
       first))



(comment

  (get-all-matchups 16)
  (get-sunday-for-week 12)

  (count (get-matchups-by-schedule 6 "2015-10-18T16:00" "2015-10-18T17:00"))
  (->> (map :time (get-matchups-by-schedule 6 nil nil))
       (map #(str/replace % #"T.*" ""))
       frequencies
       (sort-by val)
       reverse
       first
       first))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Services that hit the FFN API - try to avoid using these too often.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn populate-teams
  "Get the teams information and save it to the database."
  []
  (db/save-teams (ffn/get-teams)))

(defn populate-schedule
  "Get the season schedule and save it to the database."
  []
  (db/save-season-schedule (ffn/get-season-schedule)))

(comment
 (populate-schedule)
 (populate-teams))
