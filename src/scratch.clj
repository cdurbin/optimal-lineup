(ns scratch
  (:require
   [clj-http.client :as client]
   [clojure.string :as str]))

(def base-url
  "http://challenge.shopcurbside.com/")

(defn get-session-id
  "Get new session-id"
  []
  (:body (client/get (str base-url "get-session"))))

(defn traverse-next-node!
  "Recursively traverse along the nodes and print out the secret string as you go."
  [session-id starting-node]
  (let [response (client/get
                   (str base-url starting-node) {:headers {:session session-id} :as :json})
        ;; Needed to handle variations on :next such as :neXT
        sanitized-body (into {}
                             (for [[k v] (:body response)]
                               [(keyword (str/lower-case (name k))) v]))
        next-nodes (:next sanitized-body)]
    (if (string? next-nodes)
      (traverse-next-node! (get-session-id) next-nodes)
      (doseq [next-node next-nodes]
        (traverse-next-node! (get-session-id) next-node)))
    (when-not (= "" (str (:secret sanitized-body)))
      (println (:secret sanitized-body)))))

(comment

  ;; Ran this code from my repl and read the response.
  (traverse-next-node! (get-session-id) "/start"))

  ;; Things I would change for production code:
  ;; 1) Return the full string from traverse-next-node! rather than printing it out
  ;; 2) Add error handling to only get a new session-id when the old session-id was invalid rather
  ;;    than obtaining a new one for every call.
  ;; 3) Refactor to use loop/recur rather than direct recursion.

; Senior Software Engineer | SONIAN | REMOTE or Waltham MA
; At Sonian, we provide a hosted service for archiving, search, and analytics. Key Responsibilities •Be on a team that values code quality, good communication and collaboration, sound testing practices. •Work w Product Owners, Scrum Masters and other team members to execute against a well defined roadmap. •Architect and implement distributed and concurrent systems capable of processing data at large scale, with built-in transparency for performance monitoring and auto-scaling. •Adapt current data ingestion pipeline for new data types. •Build well documented, easy to use REST APIs and command line tools.
; Qualifications •Bachelor’s Degree in CS or equivalent. •5+ years experience building distributed systems. •Experience working in a remote team preferred
; Core Team Development: ◦Clojure ◦ElasticSearch, RabbitMQ, PostgreSQL, ZooKeeper ◦Chef ◦Agile/Scrum via Jira/Confluence/Git
; Our stack:  ◦GNU/Linux (Ubuntu) ◦Clojure, Elasticsearch, RabbitMQ, PostgresSQL, Java ◦JavaScript (ES6/ES2015), React, Ruby, Ruby on Rails, Sass/CSS3, HTML 5 ◦Chef, Docker, Sensu, Logentries  Email: jobs@sonian.net Subject: “Senior Software Engineer - Core/Backend”
