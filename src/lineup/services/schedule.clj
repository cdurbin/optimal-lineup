(ns lineup.services.schedule
  "Functions related to the NFL schedule."
  (:require [lineup.data.ffn :as ffn]
            [lineup.data.database :as db]))

(defn get-matchups-by-schedule
  "Return the teams by their start and end time."
  ([week]
   (get-matchups-by-schedule week nil nil))
  ([week start-time]
   (get-matchups-by-schedule week start-time nil))
  ([week start-time end-time]
   (db/get-matchups week start-time end-time)))

(comment
  (count (get-matchups-by-schedule 6 "2015-10-18T16:00" "2015-10-18T17:00"))
  (count (get-matchups-by-schedule 6 nil nil))
  )

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


