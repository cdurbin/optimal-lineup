(ns lineup.services.players
  (require [clojure.string :as str]
           [lineup.data.database :as db]))

(def min-defense-salary 4000)
(def min-player-salary 4500)
(def two-min-salaries (+ min-player-salary min-defense-salary))
(def three-min-salaries (+ min-player-salary min-player-salary min-defense-salary))

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
  "Only keep n players with the same salary and projected points"
  [players n]
  (flatten (for [[k v] (group-by :salary players)] (take n v))))

(defn sort-by-salary
  "Sort map of players by salary ascending"
  [players]
  (sort-by (juxt :salary :projection) players))

(defn sort-by-value
  "Sort players by value (projected points divided by salary) descending."
  [players]
  (reverse (sort-by (juxt #(/ (:projection %) (:salary %)) :projection) players)))

(defn eliminate-players
  "Get rid of any players who could not possibly be optimal"
  [players n]
  (-> (for [p players :when (potential-player? p players n)] p)
      (remove-duplicate-salaries n)
      sort-by-salary))

(defn find-eligible-players
  "Get a list of all of the players to consider for lineups for the given week. Ensures players
  have a positive value for the provided field."
  [week field]
  (->> (db/get-db-players week)
       (remove #(or (= "Out" (:game_status %))
                    (= "Doubtful" (:game_status %))
                    (= "Suspended" (:game_status %))))
       (filter #(> (field %) 0))
       (map #(assoc % :position (keyword (str/lower-case (:position %)))))
       (map #(assoc % :projection (field %)))
       (map #(select-keys % [:position :name :salary :projection]))
       (filter #(>= (:projection %) 2))))

