(ns ring.middleware.ssl
  "Middleware for managing handlers operating over HTTPS."
  (:require [ring.util.response :as resp]
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
         (assert (contains? #{"http" "https" "ws" "wss"} scheme))
         (handler (assoc req :scheme (keyword scheme)))))))

(defn- get-request? [{method :request-method}]
  (or (= method :head)
      (= method :get)))

(defn- secure-request? [{scheme :scheme}]
  (or (= scheme :https)
      (= scheme :wss)))

(defn- secure-url [{:keys [query-string scheme]
                    :as   request}
                   port]
  {:pre [(or (= :http scheme) (= :ws scheme))]}
  (str (name scheme)
       "s://"
       (-> (get-in request [:headers "host"])
         (str/split #":")
         (first))
       (when port
         (str ":" port))
       (:uri request)
       (when query-string
         (str "?" query-string))))

(defn wrap-ssl-redirect
  "Middleware that redirects any HTTP request to the equivalent HTTPS URL.

  Accepts the following options:

  :ssl-port - the SSL port to use for redirects, defaults to 443."
  {:arglists '([handler] [handler options])}
  [handler & [{:keys [ssl-port]}]]
  (fn [request]
    (if (secure-request? request)
      (handler request)
      (-> (resp/redirect (secure-url request ssl-port))
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
