(ns lineup.services.team
  (require [clojure.string :as str]
           [lineup.services.players :as players]))

(def max-total-salary 60000)

(def num-players-by-position
  "Map containing positions to the number of players of that position per team"
  {:qb 1 :rb 2 :wr 3 :te 1 :k 1 :def 1})

(defn potential-players-by-position
  "Returns a map of positions to a list of potential optimal players for that position."
  [players]
  (into {} (for [[k v] (group-by :position players)]
             {k (players/eliminate-players v (k num-players-by-position))})))

(defn optimal-team
  "Return an optimal team"
  [players]
)