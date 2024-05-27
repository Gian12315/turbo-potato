(ns servidor.handler
  (:require
   [ring.logger :refer [wrap-with-logger]]
   [clojure.tools.logging :as logging]
   [servidor.controller :as controller]
   [compojure.core :refer :all]
   [compojure.route :as route]
   [compojure.coercions :refer [as-int]]
   [ring.util.response :refer [response]]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response wrap-json-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.util.response :as res]))

(defmacro JSON-GET [& body]
  `(wrap-json-body
   (GET ~@body)))

;; Equivalent to
;; (def app-routes
;;   (routes
;;     (GET "/foo" [] "Hello Foo")))
(defroutes app-routes
  (GET "/" []
    (controller/index))
  
  (GET "/access" []
    (controller/access))

  (GET "/access/last" []
    (controller/access-last))
      
  (POST "/access" [person_type access_type]
    (controller/access-insert person_type access_type))

  (GET "/access/:year" [year :<< as-int]
    (controller/access-year year))

  (GET "/access/:year/:month" [year :<< as-int month :<< as-int]
    (controller/access-month year month))

  (GET "/access/:year/:month/:day" [year :<< as-int month :<< as-int day :<< as-int is-week]

    (if (nil? is-week)
      (controller/access-date year month day)
      (controller/access-week year month day)))

  (GET "/images" []
    (controller/images))

  (GET "/images/last" []
    (controller/images-last))

  (GET "/images/pending" []
    (controller/images-pending))

  (GET "/images/some" [type sent]
    (controller/images-some type sent))
  
  (POST "/images/insert" [:as {data :params}]
    (println "==============")
    (println (get data "type"))
    (println (:tempfile (get data "type")))
    (println (get data "description"))
    (println "==============")
    (controller/images-insert (get data "type") (:tempfile (get data "image")) (get data "description")))

  (route/files "/images" {:root "images"})
  
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true})
      wrap-multipart-params
      wrap-with-logger
      (wrap-defaults api-defaults)))
