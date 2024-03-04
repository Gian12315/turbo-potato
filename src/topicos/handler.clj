(ns topicos.handler
  (:require
   [cheshire.core :as json]
   [next.jdbc :as jdbc]
   [honey.sql :as sql]
   [honey.sql.helpers :refer :all :as h]
   [compojure.core :refer :all]
   [compojure.route :as route]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
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

(defn insert-metrics [humidity]
  (let [timestamp (java.time.LocalDateTime/ofInstant (java.time.Instant/now) (java.time.ZoneId/of "Mexico/General"))
        insert-query (sql/format {:insert-into [:metrics]
                                  :columns [:humidity :time]
                                  :values [[humidity (str (.toString timestamp) "+00")]]})]
    (println insert-query)
    (jdbc/execute! ds insert-query)))

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

  (POST "/insert" [humidity]
    (insert-metrics humidity)
    (str "OK"))

  (route/not-found "Not Found"))

(defn setup-app []
  ;; Create database
  (jdbc/execute! ds ["
CREATE TABLE IF NOT EXISTS metrics (
  id INT PRIMARY KEY AUTO_INCREMENT,
  humidity FLOAT NOT NULL,
  time TIMESTAMP WITH TIME ZONE NOT NULL
)"]))

(def app
  (wrap-defaults app-routes api-defaults))
