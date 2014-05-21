(ns ring.middleware.ssl
  "Middleware for managing handlers operating over HTTPS."
  (:require [ring.util.response :as resp]
            [ring.util.request :as req]
            [clojure.string :as str]))

(def default-scheme-header
  "The default header used in wrap-forwarded-scheme (x-forwarded-proto)."
  "x-forwarded-proto")

(defn wrap-forwarded-scheme
  "Middleware that changes the :scheme of the request map to the value present
  in a request header. This is useful if your application sits behind a
  reverse proxy or load balancer that handles the SSL transport.

  The header defaults to x-forwarded-proto."
  ([handler]
     (wrap-forwarded-scheme handler default-scheme-header))
  ([handler header]
     (fn [req]
       (let [header  (str/lower-case header)
             default (name (:scheme req))
             scheme  (str/lower-case (get-in req [:headers header] default))]
         (assert (or (= scheme "http") (= scheme "https")))
         (handler (assoc req :scheme (keyword scheme)))))))

(defn- get-request? [{method :request-method}]
  (or (= method :head)
      (= method :get)))

(defn- request-url
  "Return the full URL of the request."
  [request]
  (str (-> request :scheme name)
       "://"
       (:server-name request)
       (if-let [port (:server-port request)]
         (if-not (#{80 443} port) (str ":" port)))
       (:uri request)
       (if-let [query (:query-string request)]
         (str "?" query))))

(defn wrap-ssl-redirect
  "Middleware that redirects any HTTP request to the equivalent HTTPS URL.

  Accepts the following options:

  :ssl-port - the request should be redirected to this port (defaults to the
              port of the original request)"
  [handler & [{:as options}]]
  (fn [request]
    (if (= (:scheme request) :https)
      (handler request)
      (-> request
          (assoc :scheme :https)
          (assoc :server-port (or (:ssl-port options) (:server-port request)))
          (request-url)
          (resp/redirect)
          (resp/status   (if (get-request? request) 301 307))))))

(defn- build-hsts-header
  [{:keys [max-age include-subdomains?]
    :or   {max-age 31536000, include-subdomains? true}}]
  (str "max-age=" max-age
       (if include-subdomains? "; includeSubDomains")))

(defn wrap-hsts
  "Middleware that adds the Strict-Transport-Security header to the response
  from the handler. This ensures the browser will only use HTTPS for future
  requests to the domain.

  Accepts the following options:

  :max-age             - the max time in seconds the HSTS policy applies
                         (defaults to 31536000 seconds, or 1 year)

  :include-subdomains? - true if subdomains should be included in the HSTS
                         policy (defaults to true)

  See RFC 6797 for more information (https://tools.ietf.org/html/rfc6797)."
  {:arglists '([handler] [handler options])}
  [handler & [{:as options}]]
  (fn [request]
    (-> (handler request)
        (resp/header "Strict-Transport-Security" (build-hsts-header options)))))
