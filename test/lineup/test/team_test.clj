(ns lineup.test.team-test
  (:require [clojure.test :refer :all]
            [lineup.services.players :as players]
            [lineup.services.team :as team]
            [clojure.test.check.generators :as gen]
            [lineup.test.utils :as utils]))

(comment
  (utils/random-players-manual-check 500 2)

  (team/potential-players-by-position (repeatedly 100 utils/random-player))
  )