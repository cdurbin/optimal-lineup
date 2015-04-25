(ns lineup.test.core-test
  (:require [clojure.test :refer :all]
            [lineup.services.players :as players]
            [clojure.test.check.generators :as gen]
            [lineup.test.utils :as utils]))

(def ^:private sample-players
  [{:name "Joe"
    :salary 500
    :projection 25}
   {:name "John"
    :salary 1000
    :projection 30}
   {:name "Bob"
    :salary 400
    :projection 27}])

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

(deftest potential-players-test
  (testing "Eliminate players"
    (is (= #{"Bob" "John"}
           (set (map :name (players/eliminate-players sample-players))))))
  (testing "Multiple at same projection"
    (is (= #{"Twin1" "Twin2"}
           (set (map :name (players/eliminate-players
                             [{:name "Twin1" :projection 30 :salary 4500}
                              {:name "Twin2" :projection 30 :salary 4500}
                              {:name "Worse" :projection 20 :salary 4500}
                              {:name "Expensive" :projection 30 :salary 9000}])))))))

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
           (players/lineup->string sample-team))))
  (testing "Get total salary"
    (is (= 60000 (players/lineup->total-salary sample-team))))
  (testing "Get total projected points"
    (is (= 168.5 (players/lineup->total-projected-points sample-team)))))


(defn random-players-manual-check
  [num-players]
  (let [players (-> (repeatedly num-players utils/random-player))
        potential-players (players/eliminate-players players)
        eliminated-players (clojure.set/difference (set players) (set potential-players))]
    ; {:potential-players potential-players :eliminated-players eliminated-players}))
    {:potential-players potential-players}))

(comment
  (random-players-manual-check 1500)
  )
;; Generative tests

(deftest random-lineup
  (testing "Players get removed correctly"
    (let [players (-> (repeatedly 50 utils/random-player))
          potential-players (players/eliminate-players players)
          eliminated-players (clojure.set/difference (set players) (set potential-players))]
      {:potential-players potential-players :eliminated-players eliminated-players})))




