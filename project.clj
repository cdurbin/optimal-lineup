(defproject cdd/lineup "0.1.0-SNAPSHOT"
  :description "Optimal lineup application"
  ; :url "FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/test.check "0.7.0"]]

  :plugins [[lein-test-out "0.3.1"]
            [lein-exec "0.3.2"]]

  :profiles
  {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10"]
                        [org.clojars.gjahad/debug-repl "0.3.3"]]
         :source-paths ["src" "dev" "test"]}})