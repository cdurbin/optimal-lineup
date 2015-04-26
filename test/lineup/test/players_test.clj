(ns lineup.test.players-test
  (:require [clojure.test :refer :all]
            [lineup.services.players :as players]
            [clojure.test.check.generators :as gen]
            [lineup.test.utils :as utils]))

(def ^:private sample-players
  [{:name "Twin1" :projection 30 :salary 4500}
   {:name "Twin2" :projection 30 :salary 4500}
   {:name "Worse" :projection 20 :salary 4500}
   {:name "Expensive" :projection 30 :salary 9000}])



(deftest potential-players-test
  (testing "Multiple at same projection - need one player at position"
    (is (= (or #{"Twin1"} #{"Twin2"})
           (set (map :name (players/eliminate-players sample-players 1))))))
  (testing "Need two players at position"
        (is (= #{"Twin1" "Twin2"}
           (set (map :name (players/eliminate-players sample-players 2))))))
  (testing "Need three players at position"
        (is (= #{"Twin1" "Twin2" "Worse" "Expensive"}
           (set (map :name (players/eliminate-players sample-players 3)))))))


(comment
  (players/eliminate-players [{:name "Twin1" :projection 30 :salary 4500}
                              {:name "Twin2" :projection 30 :salary 4500}
                              {:name "Worse" :projection 20 :salary 4500}
                              {:name "Expensive" :projection 30 :salary 9000}]
                             3))

;; Generative tests

(deftest random-lineup
  (testing "Players get removed correctly"
    (let [players (-> (repeatedly 50 utils/random-player))
          potential-players (players/eliminate-players players 1)
          eliminated-players (clojure.set/difference (set players) (set potential-players))]
      {:potential-players potential-players :eliminated-players eliminated-players})))




