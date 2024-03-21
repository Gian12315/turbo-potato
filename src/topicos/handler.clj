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

(def db {:dbtype "sqlite"
         :dbname "database"})

(def ds (jdbc/get-datasource db))

;; Testing only
(def sqlmap {:select [:humidity :time]
             :from [:metrics]})

(def sqlmap2 {:select [:humidity :time]
              :from [:metrics]
              :order-by [[:id :desc]]
              :limit 1
              })


(defn render-index [name]
  (str "<h1>Hello " name "</h1><p>Bye</p>"))

(defn get-data []
  (jdbc/execute! ds (sql/format sqlmap)))

(defn get-data2 []
  (jdbc/execute! ds (sql/format sqlmap2)))

(defn render-data []
  (json/generate-string (get-data)))

(defn render-data-one []
  (json/generate-string (get-data2)))

(defn insert-metrics [humidity]
  (let [timestamp (java.time.LocalDateTime/ofInstant (java.time.Instant/now) (java.time.ZoneId/of "Mexico/General"))
        insert-query (sql/format {:insert-into [:metrics]
                                  :columns [:humidity :time]
                                  :values [[humidity timestamp]]})]
    (println insert-query)
    (jdbc/execute! ds insert-query)))

(defmacro JSON-GET
  "Define a compojure route with a JSON response"
  [& body]
  `(wrap-json-response (GET ~@body)))

(defmacro JSON-POST
  "Define a compojure route with a JSON response"
  [& body]
  `(wrap-json-body (POST ~@body)))

(defmacro te
  [& body]
  'body)

;; Equivalent to
;; (def app-routes
;;   (routes
;;     (GET "/foo" [] "Hello Foo")))
(defroutes app-routes
  (JSON-GET "/data" []
     (render-data))
  
  (JSON-GET "/recuperar" []
     (render-data))

  (JSON-GET "/recuperarUno"[]
                        (render-data-one))

  (POST "/superpost" [humidity :as req]
    (println req)
    (println (if (nil? humidity) "No llego nada wey" humidity))
    (when (not (nil? humidity))
      (insert-metrics humidity)
      (str "OK")))

  (route/not-found "Not Found"))

(defn setup-app []
  ;; Create database
  (jdbc/execute! ds ["
CREATE TABLE IF NOT EXISTS metrics (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  humidity FLOAT NOT NULL,
  time DATETIME NOT NULL
)"]))

(def app
  (wrap-defaults app-routes api-defaults))
