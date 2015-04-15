(ns lineup.core-test
  (:require [clojure.test :refer :all]
            [lineup.data.players :as players]))

(def sample-players
  [{:name "Joe"
    :salary 500
    :projection 25}
   {:name "John"
    :salary 1000
    :projection 30}
   {:name "Bob"
    :salary 400
    :projection 27}])

(deftest first-real-test
  (testing "Eliminate players"
    (is (= #{"Bob" "John"})
           #{(map :name (players/eliminate-players sample-players))})))
