(ns user
  (:require [clojure.pprint :refer (pprint pp)]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [dev.util :as util]
            [ws.api.web-server :as web]
            [lineup.api.routes :as routes]
            [lineup.system :as system])
  (:use [clojure.test :only [run-all-tests]]
        [clojure.repl]
        ;; Needed to make debug-repl available
        [alex-and-georges.debug-repl]))

(def system nil)

(defn start
  "Starts the current development system."
  []
  (let [web-server (web/create-web-server system/port (util/wrap-api routes/make-api))
        s (assoc (system/create-system) :web web-server)]
    (alter-var-root #'system
                    (constantly
                      (system/start s))))
  (util/touch-user-clj))

(defn stop
  "Shuts down and destroys the current development system."
  []
  (alter-var-root #'system
                  (fn [s] (when s (system/stop s)))))

(defn reset []
  "Reload the code and reset anything stateful"
  ; Stops the running code
  (stop)
  ; Refreshes all of the code and then restarts the system
  (refresh :after 'user/start))

(println "Custom lineup user.clj loaded.")