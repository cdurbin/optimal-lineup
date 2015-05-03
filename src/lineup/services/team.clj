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

(defn optimal-team
  "Return an optimal team"
  [players]
  (let [player-maps (potential-players-by-position players)
        best-projected-points (atom 0)
        best-lineup (atom {})]
    (doall (for [qb (:qb player-maps)
                 rb1 (:rb player-maps)
                 rb2 (:rb player-maps)
                 wr1 (:wr player-maps)
                 wr2 (:wr player-maps)
                 wr3 (:wr player-maps)
                 te (:te player-maps)
                 defense (:def player-maps)
                 k (:k player-maps)
                 :let [projected-points (+ (:projection qb) (:projection rb1) (:projection rb2)
                                           (:projection wr1) (:projection wr2) (:projection wr3)
                                           (:projection te) (:projection defense) (:projection k))]
                 :when (and (not= (:name rb1) (:name rb2))
                            (not= (:name wr1) (:name wr2))
                            (not= (:name wr2) (:name wr3))
                            (not= (:name wr1) (:name wr3))
                            (>= max-total-salary (+ (:salary qb) (:salary rb1) (:salary rb2)
                                                    (:salary wr1) (:salary wr2) (:salary wr3)
                                                    (:salary te) (:salary defense) (:salary k)))
                            (<= @best-projected-points projected-points))]
             (do
               (reset! best-projected-points projected-points)
               (reset! best-lineup {:qb qb :rb1 rb1 :rb2 rb2 :wr1 wr1 :wr2 wr2 :wr3 wr3 :te te
                                    :def defense :k k}))))
    @best-lineup))

(defn optimal-team-with-optimizations
  "Return an optimal team"
  [players]
  (let [player-maps (into {} (for [[k v] (potential-players-by-position players)]
                               {k (players/sort-by-salary v)}))
        best-projected-points (atom 0)
        best-lineup (atom {})
        max-value-by-pos (into {} (for [[k v] player-maps]
                           {k (apply max (map :projection v))}))]
    (println max-value-by-pos)

    (doall (for [qb (:qb player-maps)
                 rb1 (:rb player-maps)
                 wr1 (:wr player-maps)
                 te (:te player-maps)
                 defense (:def player-maps)
                 k (:k player-maps)]
             (doall (for [rb2 (:rb player-maps)
                          :when (not= (:name rb1) (:name rb2))
                          :while (and (>= max-total-salary (+ (:salary qb) (:salary rb1) (:salary rb2)
                                                              (:salary wr1) (:salary te) (:salary defense)
                                                              (:salary k) players/two-min-salaries))
                                      (<= @best-projected-points
                                          (+ (:projection qb) (:projection rb1)
                                             (:rb max-value-by-pos) (:projection wr1)
                                             (:wr max-value-by-pos) (:wr max-value-by-pos)
                                             (:projection te) (:projection defense)
                                             (:projection k))))]

                      (doall (for [wr2 (:wr player-maps)
                                   :when (not= (:name wr1) (:name wr2))
                                   :while (>= max-total-salary (+ (:salary qb) (:salary rb1) (:salary rb2)
                                                                  (:salary wr1) (:salary te) (:salary defense)
                                                                  (:salary k) (:salary wr2) players/min-salary))]
                               (doall (for [wr3 (:wr player-maps)
                                            :let [projected-points (+ (:projection qb) (:projection rb1)
                                                                      (:projection rb2) (:projection wr1)
                                                                      (:projection wr2) (:projection wr3)
                                                                      (:projection te) (:projection defense)
                                                                      (:projection k))]
                                            :when (and (not= (:name wr1) (:name wr3))
                                                       (not= (:name wr2) (:name wr3))
                                                       (<= @best-projected-points projected-points))
                                            :while (and (>= max-total-salary (+ (:salary qb) (:salary rb1) (:salary rb2)
                                                                                (:salary wr1) (:salary wr2) (:salary wr3)
                                                                                (:salary te) (:salary defense)
                                                                                (:salary k))))]
                                        (do
                                          (reset! best-projected-points projected-points)
                                          (reset! best-lineup {:qb qb :rb1 rb1 :rb2 rb2 :wr1 wr1 :wr2 wr2 :wr3 wr3 :te te
                                                               :def defense :k k}))))))))))
    @best-lineup))