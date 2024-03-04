(ns topicos.handler
  (:require
   [cheshire.core :as json]
   [next.jdbc :as jdbc]
   [honey.sql :as sql]
   [honey.sql.helpers :refer :all :as h]
   [compojure.core :refer :all]
   [compojure.route :as route]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(def db {:dbtype "h2"
         :dbname "database"})

(def ds (jdbc/get-datasource db))

;; Testing only
(def sqlmap {:select [:humidity :time]
             :from [:metrics]})

(defn render-index [name]
  (str "<h1>Hello " name "</h1><p>Bye</p>")
  )

(defn get-data []
  (jdbc/execute! ds (sql/format sqlmap)))

(defn render-data []
  (json/generate-string (get-data)))

;; Equivalent to
;; (def app-routes
;;   (routes
;;     (GET "/foo" [] "Hello Foo")))
(defroutes app-routes
  (wrap-json-response 
   (GET "/data" []
     (render-data)))
  (GET "/:name" [name]
    (render-index name))

  (route/not-found "Not Found"))

(defn setup-app []
  ;; Create database
  (jdbc/execute! ds ["
CREATE TABLE IF NOT EXISTS metrics (
  id INT PRIMARY KEY AUTO_INCREMENT,
  humidity FLOAT NOT NULL,
  time TIMESTAMP NOT NULL
)"])

  ;; Remove after initial tests
  (jdbc/execute! ds ["INSERT INTO metrics(humidity, time) VALUES (0.50, '2023-02-28 14:15:00')"])
  )

(def app
  (wrap-defaults app-routes site-defaults))
