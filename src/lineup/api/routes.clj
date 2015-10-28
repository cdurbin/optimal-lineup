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
    [lineup.services.errors :as errors]))

;; Routes I will need

;; Update player projection

;; Get optimal lineup based on projection

(defn- build-routes [system]
  (routes
    (GET "/testing" {:keys [headers params request-context]}
      {:status 200
       :body "I got here"})
    (route/not-found "Not Found")))

(defn make-api [system]
  (-> (build-routes system)
      keyword-params/wrap-keyword-params
      nested-params/wrap-nested-params
      errors/invalid-url-encoding-handler
      ring-json/wrap-json-body
      params/wrap-params))