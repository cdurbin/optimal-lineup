(ns lineup.data.ffn
  "Functions for retrieving data from the FFN site"
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [camel-snake-kebab.core :as csk]))

(def api-key
  "Key used when requesting data from FFN"
  "***REMOVED***")
  ; "test")

(def base-url
  "Base URL for FFN API."
  "http://www.fantasyfootballnerd.com/service")

(def api-format
  "Format requested from FFN API."
  "json")

(defn construct-url
  [service]
  (format "%s/%s/%s/%s" base-url service api-format api-key))

(defn get-season-schedule
  "Return the NFL schedule for the season."
  []
  (-> (http/get (construct-url "schedule") {:as :json})
      :body
      :Schedule))

(defn get-teams
  "Get the 32 NFL teams information."
  []
  (-> (http/get (construct-url "nfl-teams") {:as :json})
      :body
      :NFLTeams))
