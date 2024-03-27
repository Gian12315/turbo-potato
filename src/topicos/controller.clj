(ns topicos.controller
  (:require
   [cheshire.core :as json]
   [clojure.spec.alpha :as s]
   [ring.util.response :as res]
   [clojure.spec.gen.alpha :as gen]
   [topicos.database :as db]
   [topicos.util :as util]
   [clojure.tools.logging :as logging]))

(s/def :data/humidity (s/and number? #(> % 0) #(< % 100)))
(s/def :data/humidity-json (s/keys :req-un [:data/humidity]))

(s/def :data/type string?)
(s/def :data/description string?)
(def base64-regex #"^[A-Za-z0-9_-]+$")
(s/def :data/url #(re-matches base64-regex %))
(s/def :data/sent boolean?)
(s/def :data/image-json (s/keys :req-un [:data/type :data/url]
                                :opt-un [:data/description]))
(s/def :data/image-query (s/keys :req-un [(or :data/type :data/sent)]))

(defn index []
  (res/response (str "Hello")))

(defn metrics []
  (res/response (db/select-all-metrics)))

(defn metrics-last []
  (res/response (db/select-last-metric)))

(defn metrics-insert [json]
  (try
    (let [value (s/conform :data/humidity-json json)]
      (when (s/invalid? value)
        (throw (IllegalArgumentException.)))
      
      (db/insert-metric (:humidity json))
      (res/status (metrics-last) 201))
    
    (catch IllegalArgumentException _
      (-> (s/explain-str :data/humidity-json json)
          res/response
          (res/status 400)))))

(defn images []
  (res/response (db/select-all-images)))

(defn images-last []
  (res/response (db/select-last-image)))

(defn images-some [json]
  (try
    (when (s/invalid? (s/conform :data/image-query json))
      (throw (IllegalArgumentException.)))

    
    (res/response (db/select-some-images (:type json) (:sent json)))
    
    (catch IllegalArgumentException _
      (-> (s/explain-str :data/image-query json)
          res/response
          (res/status 400)))
    ))

(defn images-insert [json]
  (try
    (when (s/invalid? (s/conform :data/image-json json))
      (throw (IllegalArgumentException.)))

    (db/insert-image (:type json) (:url json) (:description json))
    (res/created "Imaged saved")
    
    (catch IllegalArgumentException _
      (-> (s/explain-str :data/image-json json)
          res/response
          (res/status 400)))
    )
  )

