(defproject ring/ring-ssl "0.3.0"
  :description "Ring middleware for managing HTTPS requests"
  :url "https://github.com/ring-clojure/ring-ssl"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ring/ring-core "1.12.1"]]
  :plugins [[lein-codox "0.10.8"]]
  :codox {:project {:name "Ring-SSL"}
          :output-path "codox"}
  :aliases {"test-all" ["with-profile" "default:+1.10:+1.11:+1.12" "test"]}
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.4.0"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.3"]]}
   :1.11 {:dependencies [[org.clojure/clojure "1.11.3"]]}
   :1.12 {:dependencies [[org.clojure/clojure "1.12.0-alpha9"]]}})
