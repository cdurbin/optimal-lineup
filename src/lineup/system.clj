(ns lineup.system
  "Defines functions for creating, starting, and stopping the application. Applications are
  represented as a map of components. Design based on
  http://stuartsierra.com/2013/09/15/lifecycle-composition and related posts."
  (:require [component.lifecycle :as lifecycle]
            [logger.core :as log :refer (debug info warn error)]
            [ws.api.web-server :as web]
            [lineup.api.routes :as routes]))

(def
  ^{:doc "Defines the order to start the components."
    :private true}
  component-order [:log :web])

(def port
  "Port the main server listens on."
  3001)

(defn create-system
  "Returns a new instance of the whole application."
  []
  {:log (log/create-logger)
   :web (web/create-web-server port routes/make-api)})

(defn start
  "Performs side effects to initialize the system, acquire resources,
  and start it running. Returns an updated instance of the system."
  [this]
  (info "System starting")
  (let [started-system (reduce (fn [system component-name]
                                 (update-in system [component-name]
                                            #(when % (lifecycle/start % system))))
                               this
                               component-order)]
    (info "System started on port" port)
    started-system))

(defn stop
  "Performs side effects to shut down the system and release its
  resources. Returns an updated instance of the system."
  [this]
  (info "System shutting down")
  (let [stopped-system (reduce (fn [system component-name]
                                 (update-in system [component-name]
                                            #(when % (lifecycle/stop % system))))
                               this
                               (reverse component-order))]
    (info "System stopped")
    stopped-system))

