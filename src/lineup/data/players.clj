(ns lineup.data.players)

(defn potential-player?
  "Returns true if the player could be used in an optimal lineup and false otherwise. Figure out
  if there is any cheaper player with higher projected points."
  [player players]
  (not (some? (seq (remove #(or (> (:projection player) (:projection %))
                           (= (:name player) (:name %))
                           (<= (:salary player) (:projection %)))
                      players)))))


(defn eliminate-players
  "Get rid of any players who could not possibly be optimal"
  [players]
  (for [p players :when (potential-player? p players)] p))

