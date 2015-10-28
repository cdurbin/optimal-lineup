(ns lineup.runner
  "Entry point for the application. Defines a main method that accepts arguments."
  (:require [lineup.system :as system]
            [clojure.string :as string]
            [logger.core :refer (debug info warn error)])
  (:gen-class))

(defn -main
  "Starts the App."
  [& args]
  (let [system (system/start (system/create-system))]
    (info "Running lineup.")))