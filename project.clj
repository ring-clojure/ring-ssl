(defproject ring/ring-ssl "0.2.1"
  :description "Ring middleware for managing HTTPS requests"
  :url "https://github.com/ring-clojure/ring-ssl"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-core "1.3.0-RC1"]]
  :plugins [[codox "0.8.5"]]
  :codox {:project {:name "Ring-SSL"}}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}})
