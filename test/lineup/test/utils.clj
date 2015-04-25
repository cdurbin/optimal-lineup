(ns lineup.test.utils
  (:require [clojure.test :refer :all]
            [lineup.services.players :as players]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]))

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

(def positions
  "All positions"
  #{:qb
    :rb
    :wr
    :te
    :def
    :k})

(defn random-position
  "Pick a position at random"
  []
  (rand-nth (seq positions)))

(def first-names
  "Collection of all potential first names"
  (->> (slurp "resources/firstnames.txt")
       str/split-lines
       (map #(str/capitalize %))))

(def last-names
  "Collection of all potential last names"
  (-> (slurp "resources/lastnames.txt") str/split-lines))

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

;; sampling generated input range
(comment
  (random-salary)
  (random-projection)
  (-> (repeatedly 10 #(random-player {:position :qb}))
      players/lineup->string)
  (map #(* 100 %) (gen/sample random-salary 1))
  (map #(/ % 10) (gen/sample random-projection))
  (rand-nth last-names))
; --> (5 6 9 8 8 9 8 6 9 9)
; ;; not losing the optional size param
; (gen/sample five-through-nine 2)
; --> (6 6)