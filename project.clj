(defproject org.clojars.vladimirmarkovic86/dao-lib "0.3.29"
  :description "Data access object library"
  :url "http://github.com/VladimirMarkovic86/dao-lib"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojars.vladimirmarkovic86/ajax-lib "0.1.14"]
                 [org.clojars.vladimirmarkovic86/session-lib "0.2.29"]
                 [org.clojars.vladimirmarkovic86/mongo-lib "0.2.13"]
                 [org.clojars.vladimirmarkovic86/utils-lib "0.4.13"]
                 ]

  :min-lein-version "2.0.0"
  
  :source-paths ["src/clj"]
  :test-paths ["test/clj"])

