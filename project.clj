(defproject cdd/lineup "0.1.0-SNAPSHOT"
  :description "Optimal lineup application"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/test.check "0.7.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [cheshire "5.5.0"]
                 [clj-http "2.0.0"]
                 [camel-snake-kebab "0.3.2"]
                 [clj-time "0.11.0"]
                 [enlive "1.1.6"]
                 [compojure "1.4.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 ;; My stuff
                 [cdd/ws "0.1.0-SNAPSHOT"]
                 [cdd/logger "0.1.0-SNAPSHOT"]
                 [cdd/component "0.1.0-SNAPSHOT"]]

  :plugins [[lein-test-out "0.3.1"]
            [lein-exec "0.3.2"]]

  :profiles
  {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10"]
                        [org.clojars.gjahad/debug-repl "0.3.3"]]
         :source-paths ["src" "dev" "test"]}})
