(ns lineup.services.players
  (require [clojure.string :as str]))

(def min-player-salary 4500)

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

(defn eliminate-players
  "Get rid of any players who could not possibly be optimal"
  [players n]
  (-> (for [p players :when (potential-player? p players n)] p)
      (remove-duplicate-salaries n)
      sort-by-salary))

