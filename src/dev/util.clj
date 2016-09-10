(ns dev.util
  "This contains utility functions for development. One such utility:
  A special debug helper that allows the last request to be repeated.
  The debug-repl occasionally has trouble running in the thread Jetty starts. This captures requests
  that have been received and then can rerun them from the repl thread where debug-repl works better."
  (:require [clojure.java.shell :as sh]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import java.awt.datatransfer.StringSelection
           java.awt.datatransfer.Clipboard
           java.awt.Toolkit))

(defn touch-file
  [file]
  (future
    (try
      (sh/sh "touch" file)
      (catch Throwable e
        (println "Error touch" file)
        (.printStackTrace e))))
  nil)

(defn touch-user-clj
  "Touches dev/user.clj to help avoid cases where file changes are not caught by
  clojure.tools.namespace refresh."
  []
  (touch-file "dev/user.clj"))

(defn touch-files-in-dir
  "Touches all top level files in the folder."
  [dir]
  (let [d (io/file dir)
        files (seq (.listFiles d))]
    (dorun (map #(-> % str touch-file) (filter #(not (.isDirectory ^java.io.File %)) files)))))

(defn speak
  "Says the specified text outloud."
  [text]
  (sh/sh "say" "-v" "Victoria" text))

(defn copy-to-clipboard
  "Copies the string into the clipboard and returns the string"
  [s]
  (let [clipboard (.getSystemClipboard (Toolkit/getDefaultToolkit))]
    (.setContents clipboard (StringSelection. s) nil))
  s)

(def last-request-atom (atom nil))

(defn save-last-request-handler
  "A ring handler that saves the last request received in an atom."
  [f]
  (fn [request]
    (reset! last-request-atom request)
    (f request)))

(defn wrap-api [api-fn]
  "Helper that will wrap the make-api function in routes. This should be called from the user
  namespace around the routes/make-api function to add the handler."
  (fn [& args]
    (save-last-request-handler (apply api-fn args))))

(defn repeat-last-request
  "Reruns the last ring request received from within the REPL. Added to make debug-repl work
  outside of Jetty thread."
  []
  (let [system (var-get (find-var 'user/system))
        routes-fn (get-in system [:web :routes-fn])]
   (if @last-request-atom
     (let [api-fn (routes-fn system)]
       (api-fn @last-request-atom))
     (println "No last request captured to repeat"))))
