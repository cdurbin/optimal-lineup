(ns lineup.test.utils
  (:require [clojure.test :refer :all]
            [lineup.services.players :as players]
            [lineup.services.team :as team]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.set :as set]))

(def ^:private random-salary-helper
  "Return a generator which generates values between 45 and 100."
  (gen/choose 45 100))

(defn random-salary
  "Pick a random salary between 4500 and 10000"
  []
  (-> (gen/sample random-salary-helper 1)
      first
      (* 100)))

(def ^:private random-projection-helper
  "Return a generator which generates values between 0 and 250."
  (gen/choose 0 250))

(defn random-projection
  "Pick a random projection between 0 and 25 in .1 increments"
  []
  (-> (gen/sample random-projection-helper 1)
      first
      (/ 10)
      float))

(defn random-position
  "Pick a position at random"
  []
  (rand-nth (keys team/num-players-by-position)))

(def first-names
  "Collection of all potential first names"
  (->> (slurp (io/resource "test/firstnames.txt"))
       str/split-lines
       (map str/capitalize)))

(def last-names
  "Collection of all potential last names"
  (-> (slurp (io/resource "test/lastnames.txt")) str/split-lines))

(defn random-name
  "Pick a random name"
  []
  (format "%s %s" (rand-nth first-names) (rand-nth last-names)))

(defn random-player
  "Generates a random player"
  ([]
   (random-player {}))
  ([fixed-attributes]
   (merge
     {:position (random-position)
      :name (random-name)
      :salary (random-salary)
      :projection (random-projection)}
     fixed-attributes)))

(defn random-players-manual-check
  [num-players n]
  (let [players (-> (repeatedly num-players random-player))
        potential-players (players/eliminate-players players n)
        eliminated-players (set/difference (set players) (set potential-players))]
    {:potential-players potential-players}))

(comment
  (random-players-manual-check 500 2)
  (System/getenv)
  )

(comment
  (random-salary)
  (random-projection)
  (-> (repeatedly 10 #(random-player {:position :qb}))
      players/lineup->string)
  (map #(* 100 %) (gen/sample random-salary 1))
  (map #(/ % 10) (gen/sample random-projection))
  (rand-nth last-names))
