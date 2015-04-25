(ns lineup.services.players
  (require [clojure.string :as str]))

(defn potential-player?
  "Returns true if the player could be used in an optimal lineup and false otherwise. Figure out
  if there is any cheaper player with higher projected points."
  [player players]
  (not (some? (seq (remove #(or (> (:projection player) (:projection %))
                                (= (:name player) (:name %))
                                (and (= (:projection player) (:projection %))
                                     (= (:salary player) (:salary %)))
                                (< (:salary player) (:salary %)))
                           players)))))

(defn eliminate-players
  "Get rid of any players who could not possibly be optimal"
  [players]
  (for [p players :when (potential-player? p players)] p))

(def positions
  "List of positions that make up a team"
  #{:qb
    :rb1
    :rb2
    :wr1
    :wr2
    :wr3
    :te
    :k
    :def})

(def max-total-salary 60000)
(def min-player-salary 4500)

(defn lineup->string
  "Takes a collection of player maps. The keys in the map are :position :name :salary :projection.
  Returns a string representing the players"
  [players]
  (for [p players]
    (format "%s: %s Salary: [%d] Projection: [%.1f]"
            (-> (:position p) name str/upper-case)
            (:name p)
            (:salary p)
            (:projection p))))

(defn lineup->total-salary
  "Returns the total salary for a lineup"
  [players]
  (reduce + (map #(:salary %) players)))

(defn lineup->total-projected-points
  "Returns the total projected points for a lineup"
  [players]
  (reduce + (map #(:projection %) players)))



