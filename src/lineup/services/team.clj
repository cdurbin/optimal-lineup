(ns lineup.services.team
  (require [clojure.string :as str]
           [lineup.services.players :as players]
           [lineup.services.schedule :as schedule]))

(def max-total-salary (- 60000 4500))

(def num-players-by-position
  "Map containing positions to the number of players of that position per team"
  {:qb 1 :rb 2 :wr 3 :te 1 :k 1 :def 1})

(def team-positions
  [:qb :rb1 :rb2 :wr1 :wr2 :wr3 :te :k :def])

(defn potential-players-by-position
  "Returns a map of positions to a list of potential optimal players for that position."
  [players use-multiple-per-position?]
  (into {} (for [[position v] (group-by :position players)
                 :let [num-players-at-position (if use-multiple-per-position?
                                                 (position num-players-by-position)
                                                 1)]]
             {position (players/eliminate-players v num-players-at-position)})))

(defn lineup->string
  "Takes a collection of player maps. The keys in the map are :position :name :salary :projection.
  Returns a string representing the players"
  [nested-player-map]
  (for [position team-positions
        :let [player (position nested-player-map)]]
    (format "%s: %s Salary: [%d] Projection: [%.1f]"
            (-> position name str/upper-case)
            (:name player)
            (:salary player)
            (:projection player))))

(defn lineup->total-salary
  "Returns the total salary for a lineup"
  [lineup]
  (reduce + (* players/min-player-salary (- (count team-positions) (count (keys lineup))))
          (map :salary (vals lineup))))

(defn lineup->total-projected-points
  "Returns the total projected points for a lineup"
  [lineup]
  (reduce + (map :projection (vals lineup))))


(defn best-value-team
  "Returns a team of all the players with the best points per dollar projection."
  [players]
  (let [player-maps (into {} (for [[k v] (potential-players-by-position players true)]
                               {k (players/sort-by-value v)}))]
    {:qb (first (:qb player-maps))
     :rb1 (first (:rb player-maps))
     :rb2 (second (:rb player-maps))
     :wr1 (first (:wr player-maps))
     :wr2 (second (:wr player-maps))
     :wr3 (nth (:wr player-maps) 2)
     :te (first (:te player-maps))
     :def (first (:def player-maps))}))


(defn optimal-team-with-optimizations
  "Return an optimal team. Ignore kickers."
  [players]
  (let [best-value-lineup (best-value-team players)
        valid-lineup (>= (+ 4500 max-total-salary) (lineup->total-salary best-value-lineup))
        best-projected-points (atom (if valid-lineup
                                      (lineup->total-projected-points best-value-lineup)
                                      0))
        best-lineup (atom (if valid-lineup best-value-lineup {}))]
    (loop [last-top-score 0]
      (let [use-multiple-per-position? (not= 0 last-top-score)
            player-maps (into {} (for [[k v] (potential-players-by-position players use-multiple-per-position?)]
                                   {k (players/sort-by-salary v)}))
            wr1-map (drop 2 (:wr player-maps))
            wr2-map (->> (:wr player-maps)
                         rest
                         drop-last)
            wr3-map (drop-last 2 (:wr player-maps))
            rb1-map (rest (:rb player-maps))
            rb2-map (drop-last (:rb player-maps))
            player-maps (dissoc player-maps :wr :rb)
            player-maps (assoc player-maps :rb1 rb1-map :rb2 rb2-map :wr1 wr1-map :wr2 wr2-map :wr3 wr3-map)
            max-value-by-pos (into {} (for [[k v] player-maps]
                                        {k (apply max (map :projection v))}))]
        (doall (for [qb (:qb player-maps)
                     rb1 (:rb1 player-maps)
                     wr1 (:wr1 player-maps)]
                 (doall (for [rb2 (:rb2 player-maps)
                              :when (not= (:name rb1) (:name rb2))
                              :while (>= (:salary rb1) (:salary rb2))]
                          (doall (for [wr2 (:wr2 player-maps)
                                       :when (not= (:name wr1) (:name wr2))
                                       :while (and (>= (:salary wr1) (:salary wr2))
                                                   (>= max-total-salary (+ (:salary qb) (:salary rb1) (:salary rb2)
                                                                           (:salary wr1) (:salary wr2) players/three-min-salaries))
                                                   (< @best-projected-points
                                                      (+ (:projection qb) (:projection rb1)
                                                         (:projection rb2) (:projection wr1)
                                                         (:wr2 max-value-by-pos) (:wr3 max-value-by-pos)
                                                         (:te max-value-by-pos) (:def max-value-by-pos))))]
                                   (doall (for [wr3 (:wr3 player-maps)
                                                :when (and (not= (:name wr1) (:name wr3))
                                                           (not= (:name wr2) (:name wr3)))
                                                :while (and (>= (:salary wr2) (:salary wr3))
                                                            (>= max-total-salary (+ (:salary qb) (:salary rb1) (:salary rb2)
                                                                                    (:salary wr1) (:salary wr2) (:salary wr3) players/two-min-salaries))
                                                            (< @best-projected-points
                                                               (+ (:projection qb) (:projection rb1)
                                                                  (:projection rb2) (:projection wr1)
                                                                  (:projection wr2) (:wr3 max-value-by-pos)
                                                                  (:te max-value-by-pos) (:def max-value-by-pos))))]
                                            (doall (for [te (:te player-maps)
                                                         :while (and (>= max-total-salary (+ (:salary qb) (:salary rb1) (:salary rb2)
                                                                                             (:salary wr1) (:salary wr2) (:salary wr3)
                                                                                             (:salary te) players/min-defense-salary))
                                                                     (< @best-projected-points
                                                                        (+ (:projection qb) (:projection rb1)
                                                                           (:projection rb2) (:projection wr1)
                                                                           (:projection wr2) (:projection wr3)
                                                                           (:te max-value-by-pos) (:def max-value-by-pos))))]
                                                     (doall (for [defense (:def player-maps)
                                                                  :let [projected-points (+ (:projection qb) (:projection rb1)
                                                                                            (:projection rb2) (:projection wr1)
                                                                                            (:projection wr2) (:projection wr3)
                                                                                            (:projection te) (:projection defense))]
                                                                  :when (< @best-projected-points projected-points)
                                                                  :while (and (>= max-total-salary (+ (:salary qb) (:salary rb1) (:salary rb2)
                                                                                                      (:salary wr1) (:salary wr2) (:salary wr3)
                                                                                                      (:salary te) (:salary defense)))
                                                                              (< @best-projected-points
                                                                                 (+ (:projection qb) (:projection rb1)
                                                                                    (:projection rb2) (:projection wr1)
                                                                                    (:projection wr2) (:projection wr3)
                                                                                    (:projection te) (:def max-value-by-pos))))]
                                                              (do
                                                                (reset! best-projected-points projected-points)
                                                                (reset! best-lineup {:qb qb :rb1 rb1 :rb2 rb2 :wr1 wr1 :wr2 wr2 :wr3 wr3 :te te
                                                                                     :def defense}))))))))))))))
        (if-not (= last-top-score 0)
          @best-lineup
          (recur @best-projected-points))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; For reference only
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn optimal-team
  "Return an optimal team, but is not optimized. Used for reference only when testing to make sure
  new optimizations work."
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Pick optimal lineups
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-top-lineups
  "Returns top 'n' lineups for the provided type"
  [week start-time end-time scoring-field n]
  (let [unwanted-players (atom [])]
    (doseq [x (range n)]
      ;; Narrow down players
      (let [db-players (players/find-eligible-players
                         week scoring-field start-time end-time @unwanted-players)
            ;; Find best lineups
            optimal-lineup (time (optimal-team-with-optimizations db-players))]
        ;; Print out lineups
        (clojure.pprint/pprint (lineup->string optimal-lineup))
        (println (lineup->total-salary optimal-lineup))
        (println (format "%.2f" (lineup->total-projected-points optimal-lineup)))
        (reset! unwanted-players (apply conj @unwanted-players optimal-lineup))))))

(comment


  (do
    (def week 8)
    (def sunday (schedule/get-sunday-for-week week))
    (def start-time (str sunday "T23:00"))
    ; (def start-time "2015-11-01T15:00")
    ; (def end-time (str sunday "T17:00"))
    (def end-time nil)
    (get-top-lineups week start-time end-time :ppr 3))
  )





