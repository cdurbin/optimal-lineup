(ns lineup.services.errors
  "Contains functions that will throw errors that when caught in the API will return the
  correct error code. Errors can be a list of strings."
  (:require
   [cheshire.core :as json]
   [clojure.string :as str]
   [logger.core :as log :refer (debug info warn error)]))

(defn throw-service-error
  "Throws an instance of clojure.lang.ExceptionInfo that will contain a map with the type of
  error and a message. See http://stackoverflow.com/a/16159584."
  ([type msg]
   (throw (ex-info msg {:type type :errors [msg]})))
  ([type msg cause]
   (throw (ex-info msg {:type type :errors [msg]} cause))))

(defn- errors->message
  "Returns an error message to include as the message in a thrown exception."
  [errors]
  (first errors))

(defn throw-service-errors
  "Throws an instance of clojure.lang.ExceptionInfo that will contain a map with the type of
  error and errors. See http://stackoverflow.com/a/16159584."
  ([type errors]
   (throw (ex-info (errors->message errors) {:type type :errors errors})))
  ([type errors cause]
   (throw (ex-info (errors->message errors) {:type type :errors errors} cause))))

(defn internal-error!
  "Throws an Exception with the given message and error, if given, to indicate an internal error in the system."
  ([^String msg]
   (internal-error! msg nil))
  ([^String msg ^Throwable cause]
   (if cause
     (throw (Exception. msg cause))
     (throw (Exception. msg)))))

(defn handle-service-errors
  "A helper for catching and handling service errors. Takes one function that may generate a service
  error. The other function handles the service error. It will be passed three arguments: the error
  type, the list of errors, and the actual exception."
  [f error-handler]
  (try
    (f)
    (catch clojure.lang.ExceptionInfo e
      (let [{:keys [type errors]} (ex-data e)]
        (if (and type errors)
          (error-handler type errors e)
          (throw e))))))

;; TODO Move this into web server

;; Copied from common.api.errors

(def type->http-status-code
  {:not-found 404
   :bad-request 400
   :unauthorized 401
   :invalid-data 422
   :conflict 409
   :service-unavailable 503})

(def CONTENT_TYPE_HEADER "Content-Type")
(def CORS_ORIGIN_HEADER "Access-Control-Allow-Origin")

(def internal-error-ring-response
  {:status 500
   :headers {CONTENT_TYPE_HEADER :json
             CORS_ORIGIN_HEADER "*"}
   :body {:errors ["An Internal Error has occurred."]}})

(defmulti errors->body-string
  "Converts a set of errors into a string to return in the response body formatted according
  to the requested response format."
  (fn [response-format errors]
    response-format))

(defmulti error->json-element
  "Converts an individual error element to a clojure data structure representing the JSON element."
  (fn [error]
    (type error)))

(defmethod error->json-element String
  [error]
  error)

(defmethod errors->body-string :json
  [response-format errors]
  (json/generate-string {:errors (map error->json-element errors)}))

(defn- response-type-body
  "Returns the response content-type and body for the given errors and format"
  [errors results-format]
  (let [content-type "application/json"
        response-format :json
        body (errors->body-string response-format errors)]
    [content-type body]))

(defn- handle-service-error
  "Handles service errors thrown during a request and returns the appropriate ring response."
  [default-format-fn request type errors e]
  (let [results-format "application/json"
        status-code (type->http-status-code type)
        [content-type response-body] (response-type-body errors results-format)]
    ;; Log exceptions for server errors
    (if (>= status-code 500)
      (error e)
      (warn "Failed with status code [" status-code "], response body: " response-body))
    {:status status-code
     :headers {CONTENT_TYPE_HEADER content-type
               CORS_ORIGIN_HEADER "*"}
     :body response-body}))

(defn exception-handler
  "A ring exception handler that will handle errors thrown by the cmr.common.services.errors
  functions. The default-format-fn is a function which determines in what format to return an error
  if the request does not explicitly set a format.  It takes the request and the ExceptionInfo
  as arguments."
  ([f]
   (exception-handler f (constantly "application/json")))
  ([f default-format-fn]
   (fn [request]
     (try
       (handle-service-errors
         (partial f request)
         (partial handle-service-error default-format-fn request))
       (catch Throwable e
         (error e)
         internal-error-ring-response)))))

(defn invalid-url-encoding-handler
  "Detect invalid encoding in the url and throws a 400 error. Ring default handling simply converts
  the invalid encoded parameter value to nil and causes 500 error later during search (see CMR-1192).
  This middleware handler returns a 400 error early to avoid the 500 error."
  [f]
  (fn [request]
    (try
      (when-let [query-string (:query-string request)]
        (java.net.URLDecoder/decode query-string "UTF-8"))
      (catch Exception e
        (throw-service-error
          :bad-request
          (str "Invalid URL encoding: " (str/replace (.getMessage e) #"URLDecoder: " "")))))
    (f request)))
