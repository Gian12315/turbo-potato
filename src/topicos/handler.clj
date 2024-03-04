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

(def db {:dbtype (System/getenv "DBTYPE")
         :dbname (System/getenv "DBNAME")
         :user (System/getenv "DBUSER")
         :password (System/getenv "DBPASS")})

(defn get-conf []
  (let [data (json/parse-string (slurp "database.json"))]
    (clojure.core/for [row data]
      (first row))
    )
  )

(def ds (jdbc/get-datasource db))

;; Testing only
(def sqlmap {:select [:id :humedad]
             :from [:datos]})

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

(def app
  (wrap-defaults app-routes site-defaults))
