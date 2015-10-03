(defproject cdd/lineup "0.1.0-SNAPSHOT"
  :description "Optimal lineup application"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/test.check "0.7.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.xerial/sqlite-jdbc "3.7.2"]]

  :plugins [[lein-test-out "0.3.1"]
            [lein-exec "0.3.2"]]

  :profiles
  {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10"]
                        [org.clojars.gjahad/debug-repl "0.3.3"]]
         :source-paths ["src" "dev" "test"]}})
