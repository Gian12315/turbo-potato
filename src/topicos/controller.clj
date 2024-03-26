(ns topicos.controller
  (:require
   [cheshire.core :as json]
   [clojure.spec.alpha :as s]
   [ring.util.response :as res]
   [clojure.spec.gen.alpha :as gen]
   [topicos.database :as db]
   [clojure.tools.logging :as logging]))

(s/def :data/metric (s/and number? #(> % 0) #(< % 100)))

(s/def :data/type string?)
(s/def :data/url string?)
(s/def :data/image-json (s/keys :req-un [:data/type :data/url]))

(defn index []
  (res/response (str "Hello")))

(defn metrics []
  (res/response (db/select-all-metrics)))

(defn metrics-last []
  (res/response (db/select-last-metric)))

(defn metrics-insert [humidity]
  (try
    (let [val (s/conform :data/metric (Float/parseFloat humidity))] 
      (db/insert-metric val)
      (logging/info val)
      (res/status (metrics-last) 201))
    
    (catch NumberFormatException _ (res/status (res/response (str "Sent value '" humidity "' is not a number")) 400))))

