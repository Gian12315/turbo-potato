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

(def date-regex #"^(19[0-9]{2}|2[0-9]{3})-(0[1-9]|1[012])-([123]0|[012][1-9]|31)$")
(s/def :data/time #(re-matches date-regex %))
(s/def :data/time-json (s/keys :req-un [:data/time]))

(s/def :data/type string?)
(s/def :data/description string?)
(def base64-regex #"^[A-Za-z0-9_-]+$")
(s/def :data/url #(re-matches base64-regex %))
(s/def :data/sent boolean?)
(s/def :data/image-json (s/keys :req-un [:data/type :data/url]
                                :opt-un [:data/description]))
(s/def :data/image-query (s/keys :req-un [(or :data/type :data/sent)]))

(comment 
  (defn mock-data []
    (doseq [i (range 1 13) j (range 1 28) k (range 0 24)]
      (db/insert-metric-timestamp
       (gen/generate (s/gen :data/humidity))
       (java.time.LocalDateTime/of 2024 i j k 0 0)))))

(defn index []
  (res/response (str "Hello")))

(defn metrics []
  (res/response (db/select-all-metrics)))

(defn metrics-last []
  (res/response (db/select-last-metric)))

(defn metrics-year [year]
  (let [startDate (format "%d-00-00" year)
        endDate (format "%d-12-31" year)]

    (res/response (db/select-range-metrics startDate endDate))))

(defn metrics-month [year month]
  (let [startDate (format "%d-%02d-00" year month)
        endDate (format "%d-%02d-31" year month)]

    (res/response (db/select-range-metrics startDate endDate))))

(defn metrics-week [year month week]
  (let [startDate (format "%d-%02d-%02d" year month (* (- week 1) 7))
        endDate (format "%d-%02d-%02d" year month (* week 7))]

    (res/response (db/select-range-metrics startDate endDate))))

(defn metrics-date [year month day]
  (res/response (db/select-date-metrics (format "%d-%02d-%02d" year month day))))

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

(defn images-pending []
  (res/response (db/select-pending-images)))

(defn images-some [json]
  (try
    (when (s/invalid? (s/conform :data/image-query json))
      (throw (IllegalArgumentException.)))


    (let [{:keys [type sent]} json]
      (res/response (db/select-some-images type sent)))
    
    
    (catch IllegalArgumentException _
      (-> (s/explain-str :data/image-query json)
          res/response
          (res/status 400)))
    ))

(defn images-insert [json]
  (try
    (when (s/invalid? (s/conform :data/image-json json))
      (throw (IllegalArgumentException.)))

    (let [{:keys [type url description]} json]
      (db/insert-image type url description))
    
    (res/created "Hi")
    
    (catch IllegalArgumentException _
      (-> (s/explain-str :data/image-json json)
          res/response
          (res/status 400)))
    )
  )

