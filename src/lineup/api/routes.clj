(ns lineup.api.routes
  "Defines the HTTP URL routes (using Compojure)."
  (:require
    [compojure.route :as route]
    [compojure.core :refer :all]
    [ring.util.response :as r]
    [ring.middleware.json :as ring-json]
    [ring.middleware.params :as params]
    [ring.middleware.nested-params :as nested-params]
    [ring.middleware.keyword-params :as keyword-params]
    [ring.middleware.resource :as resource]
    [ring.middleware.content-type :as content-type]
    [ring.middleware.not-modified :as not-modified]
    [lineup.services.errors :as errors]
    [clojure.java.io :as io]))

;; Routes I will need

;; Update player projection

;; Get optimal lineup based on projection

(defn- build-routes [system]
  (routes
    (GET "/testing" {:keys [headers params request-context]}
      {:status 200
       :body "I got here"
       :headers {"Content-type" "text/html"}})
    (route/not-found "Not Found")))

(defn make-api [system]
  (-> (build-routes system)
      keyword-params/wrap-keyword-params
      nested-params/wrap-nested-params
      errors/invalid-url-encoding-handler
      ring-json/wrap-json-body
      params/wrap-params
      (resource/wrap-resource "public")
      content-type/wrap-content-type
      not-modified/wrap-not-modified))