(ns lineup.services.players
  (require [clojure.string :as str]))

(defn potential-player?
  "Returns true if the player could be used in an optimal lineup and false otherwise. Figure out
  if there are at least n cheaper players with higher projected points."
  [player players n]
  (not (< (dec n)
          (count (remove (fn [other] (or (> (:projection player) (:projection other))
                                         (= (:name player) (:name other))
                                         (and (= (:projection player) (:projection other))
                                              (= (:salary player) (:salary other)))
                                         (< (:salary player) (:salary other))))
                         players)))))

(defn remove-duplicate-salaries
  "Only keep 1 player with the same salary and projected points"
  [players n]
  (let [grouped-salary-map (group-by :salary players)]
    (flatten (for [[k v] grouped-salary-map] (take n v)))))

(defn eliminate-players
  "Get rid of any players who could not possibly be optimal"
  [players n]
  (-> (for [p players :when (potential-player? p players n)] p)
      (remove-duplicate-salaries n)))

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



