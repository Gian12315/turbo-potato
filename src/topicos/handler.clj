(ns topicos.handler
  (:require
   [ring.logger :refer [wrap-with-logger]]
   [clojure.tools.logging :as logging]
   [topicos.controller :as controller]
   [compojure.core :refer :all]
   [compojure.route :as route]
   [ring.util.response :refer [response]]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response wrap-json-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.util.response :as res]))


;; Equivalent to
;; (def app-routes
;;   (routes
;;     (GET "/foo" [] "Hello Foo")))
(defroutes app-routes
  (GET "/" []
    (controller/index))
  
  (GET "/metrics" []
            (controller/metrics))

  (GET "/metrics/last" []
            (controller/metrics-last))
    
  (POST "/metrics/insert" {{:keys [humidity]} :body}
    (logging/info "Received humidity value of:" humidity)
    (controller/metrics-insert humidity))

  (GET "/images/insert" [json]
    (controller/images-insert))

  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true})
      wrap-with-logger
      (wrap-defaults api-defaults)))
