(defproject topicos "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cheshire "5.12.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.1"]
                 [com.github.seancorfield/honeysql "2.5.1103"]
                 [com.github.seancorfield/next.jdbc "1.3.909"]
                 [org.xerial/sqlite-jdbc "3.43.0.0"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler topicos.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
