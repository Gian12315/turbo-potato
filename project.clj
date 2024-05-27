(defproject servidor "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.11.2"]
                 [cheshire "5.12.0"]
                 [compojure "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.1"]
                 [ring-logger "1.1.1"]
                 [org.clojure/tools.logging "1.3.0"]
                 [com.github.seancorfield/honeysql "2.5.1103"]
                 [com.github.seancorfield/next.jdbc "1.3.909"]
                 ;; https://mvnrepository.com/artifact/commons-codec/commons-codec
                 [commons-codec/commons-codec "1.16.1"]
                 [org.slf4j/slf4j-simple "2.0.12"]
                 [org.xerial/sqlite-jdbc "3.45.1.0"]]
  :plugins [[lein-ring "0.12.5"]]
  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]
  :ring {:init servidor.database/create-table
         :handler servidor.handler/app
         :port 3000}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]
                        [org.clojure/test.check "0.9.0"]]}})
