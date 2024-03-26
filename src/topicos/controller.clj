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
    (let [value (s/conform :data/metric humidity)]
      (when (s/invalid? value)
        (throw (IllegalArgumentException.)))
      
      (db/insert-metric value)
      (res/status (metrics-last) 201))
    
    (catch IllegalArgumentException _
      (-> (s/explain-str :data/metric humidity)
          res/response
          (res/status 400)))))

(defn images-insert [json]
  (try
    (when (s/invalid? (s/conform :data/image-json json))
      (throw (IllegalArgumentException.)))

    (db/insert-image json)
    
    (catch IllegalArgumentException _
      (-> (s/explain-str :data/image-json json)
          res/response
          (res/status 400)))
    )
  )

