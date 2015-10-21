(ns lineup.test.team-test
  (:require [clojure.test :refer :all]
            [lineup.services.players :as players]
            [lineup.services.team :as team]
            [clojure.test.check.generators :as gen]
            [lineup.test.utils :as utils]
            [lineup.services.schedule :as schedule]))

(def ^:private sample-team
  [{:position :qb
    :name "Joe Montana"
    :salary 7900
    :projection 24.5}
   {:position :rb1
    :name "Barry Sanders"
    :salary 9000
    :projection 30.5}
   {:position :rb2
    :name "Lesean McCoy"
    :salary 7000
    :projection 21.5}
   {:position :wr1
    :name "Jerry Rice"
    :salary 8400
    :projection 18.5}
   {:position :wr2
    :name "Art Monk"
    :salary 5500
    :projection 14.5}
   {:position :wr3
    :name "Calvin Johnson"
    :salary 8100
    :projection 21.0}
   {:position :te
    :name "Tony Gonzalez"
    :salary 4500
    :projection 20.5}
   {:position :def
    :name "Chicago Bears"
    :salary 5100
    :projection 9.5}
   {:position :k
    :name "Chip Lohmiller"
    :salary 4500
    :projection 8.0}])

(deftest lineup-test
  (testing "Print Lineup"
    (is (= ["QB: Joe Montana Salary: [7900] Projection: [24.5]"
            "RB1: Barry Sanders Salary: [9000] Projection: [30.5]"
            "RB2: Lesean McCoy Salary: [7000] Projection: [21.5]"
            "WR1: Jerry Rice Salary: [8400] Projection: [18.5]"
            "WR2: Art Monk Salary: [5500] Projection: [14.5]"
            "WR3: Calvin Johnson Salary: [8100] Projection: [21.0]"
            "TE: Tony Gonzalez Salary: [4500] Projection: [20.5]"
            "DEF: Chicago Bears Salary: [5100] Projection: [9.5]"
            "K: Chip Lohmiller Salary: [4500] Projection: [8.0]"]
           (team/lineup->string sample-team))))
  (testing "Get total salary"
    (is (= 60000 (team/lineup->total-salary sample-team))))
  (testing "Get total projected points"
    (is (= 168.5 (team/lineup->total-projected-points sample-team)))))

(comment
  (let [unwanted-std-players (atom [])
        unwanted-high-players (atom [])
        unwanted-low-players (atom[])]
    (doseq [x (range 2)]
      ;; Parameters
      (def week 6)
      (def sunday (schedule/get-sunday-for-week week))
      (def start-time (str sunday "T14:00"))
      ; (def end-time (str sunday "T17:00"))
      (def end-time nil)
      ;; Narrow down players
      (def standard-db-players (players/find-eligible-players week :ppr start-time end-time @unwanted-std-players))
      (def high-db-players (players/find-eligible-players week :ppr_high start-time end-time))
      (def safe-db-players (players/find-eligible-players week :ppr_low start-time end-time))
      ;; Find best lineups
      (def optimal-standard (time (team/optimal-team-with-optimizations standard-db-players)))
      (def optimal-high (time (team/optimal-team-with-optimizations high-db-players)))
      (def optimal-safe (time (team/optimal-team-with-optimizations safe-db-players)))
      (def best-value-lineup (time (team/best-value-team standard-db-players)))
      ;; Print out lineups
      (clojure.pprint/pprint (team/lineup->string optimal-standard))
      (println (team/lineup->total-salary optimal-standard))
      (println (format "%.2f" (team/lineup->total-projected-points optimal-standard)))

      (clojure.pprint/pprint (team/lineup->string optimal-high))
      (println (team/lineup->total-salary optimal-high))
      (println (format "%.2f" (team/lineup->total-projected-points optimal-high)))

      (clojure.pprint/pprint (team/lineup->string optimal-safe))
      (println (team/lineup->total-salary optimal-safe))
      (println (format "%.2f" (team/lineup->total-projected-points optimal-safe)))

      (reset! unwanted-std-players (apply conj @unwanted-std-players optimal-standard))

      ; (clojure.pprint/pprint (team/lineup->string best-value-lineup))
      ; (println (team/lineup->total-salary best-value-lineup))
      ; (println (format "%.2f" (team/lineup->total-projected-points best-value-lineup)))
      ))
  )



(comment
  (utils/random-players-manual-check 500 2)

  (team/potential-players-by-position (repeatedly 100 utils/random-player))

  (def the-players (repeatedly 100 utils/random-player))


  (distinct (map :position db-players))

  (def optimal-db-tweaked (time (team/optimal-team-with-optimizations db-players)))

  (team/lineup->string optimal-db-tweaked)
  (team/lineup->total-salary optimal-db-tweaked)
  (team/lineup->total-projected-points optimal-db-tweaked)


  (def baseline-optimal (time (team/optimal-team the-players)))
  (def optimal-tweaked (time (team/optimal-team-with-optimizations the-players)))
  (def dryer-optimized (time (team/dryer-optimize the-players)))


  (= baseline-optimal optimal-tweaked)

  ; (def the-team (team/optimal-team (repeatedly 80 utils/random-player)))
  (def players-on-baseline-team (for [[k v] baseline-optimal] v))
  (def players-on-team (for [[k v] optimal-tweaked] v))
  (team/lineup->string players-on-team)
  (team/lineup->total-salary players-on-team)
  (team/lineup->total-projected-points players-on-team)
  )