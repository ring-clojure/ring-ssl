(ns ring.middleware.ssl-test
  (:use clojure.test
        ring.middleware.ssl
        [ring.mock.request :only (request header)]
        [ring.util.response :only (response get-header)]))

(deftest test-wrap-forwarded-scheme
  (let [handler #(response (name (:scheme %)))]
    (let [handler (wrap-forwarded-scheme handler)]
      (testing "no header"
        (let [response (handler (request :get "/"))]
          (is (= (:body response) "http")))
        (let [response (handler (request :get "https://localhost/"))]
          (is (= (:body response) "https"))))

      (testing "default header"
        (let [response (handler (-> (request :get "/")
                                    (header "x-forwarded-proto" "https")))]
          (is (= (:body response) "https")))
        (let [response (handler (-> (request :get "https://localhost/")
                                    (header "x-forwarded-proto" "http")))]
          (is (= (:body response) "http")))))

    (testing "custom header"
      (let [handler  (wrap-forwarded-scheme handler "X-Foo")
            response (handler (-> (request :get "/")
                                  (header "x-foo" "https")))]
        (is (= (:body response) "https"))))))

(deftest test-wrap-ssl-redirect
  (let [handler (wrap-ssl-redirect (constantly (response "")))]
    (testing "HTTP GET request"
      (let [response (handler (request :get "/"))]
        (is (= (:status response) 301))
        (is (= (get-header response "location") "https://localhost/"))))
    
    (testing "HTTP POST request"
      (let [response (handler (request :post "/"))]
        (is (= (:status response) 307))
        (is (= (get-header response "location") "https://localhost/"))))
    
    (testing "HTTPS request"
      (let [response (handler (request :get "https://localhost/"))]
        (is (= (:status response) 200))
        (is (nil? (get-header response "location"))))))

  (let [handler (wrap-ssl-redirect (constantly (response "")) {:ssl-port 8443})]
    (testing "HTTP GET request with custom SSL port"
      (let [response (handler (request :get "/"))]
        (is (= (:status response) 301))
        (is (= (get-header response "location") "https://localhost:8443/"))))

    (testing "HTTP POST request with custom SSL port"
      (let [response (handler (request :post "/"))]
        (is (= (:status response) 307))
        (is (= (get-header response "location") "https://localhost:8443/")))))

  (let [handler (wrap-ssl-redirect (constantly (response "")) {:exempt-paths ["/healthcheck"]})]
    (testing "HTTP request"
      (let [response (handler (request :get "/healthcheck"))]
        (is (= (:status response) 200))
        (is (nil? (get-header response "location")))))

    (testing "HTTPS request"
      (let [response (handler (request :get "/healthcheck"))]
        (is (= (:status response) 200))
        (is (nil? (get-header response "location")))))))

(deftest test-wrap-hsts
  (testing "defaults"
    (let [handler  (wrap-hsts (constantly (response "")))
          response (handler (request :get "/"))]
      (is (= (get-header response "strict-transport-security")
             "max-age=31536000; includeSubDomains"))))
  
  (testing "custom max-age"
    (let [handler  (wrap-hsts (constantly (response "")) {:max-age 0})
          response (handler (request :get "/"))]
      (is (= (get-header response "strict-transport-security")
             "max-age=0; includeSubDomains"))))

  (testing "don't include subdomains"
    (let [handler  (wrap-hsts (constantly (response "")) {:include-subdomains? false})
          response (handler (request :get "/"))]
      (is (= (get-header response "strict-transport-security")
             "max-age=31536000")))))
