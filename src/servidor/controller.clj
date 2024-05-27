(ns servidor.controller
  (:require
   [cheshire.core :as json]
   [clojure.spec.alpha :as s]
   [ring.util.response :as res]
   [clojure.spec.gen.alpha :as gen]
   [clojure.java.io :as io]
   [servidor.database :as db]
   [servidor.util :as util]
   [clojure.tools.logging :as logging])
  (:import java.time.format.DateTimeFormatter))

(s/def :data/humidity (s/and number? #(> % 0) #(< % 100)))


(s/def :data/person_type #{"niño" "adulto"})
(s/def :data/access_type #{"entrada" "salida"})

(s/def :data/humidity-standard (s/and number? #(> % 40) #(< % 60)))

(s/def :data/type string?)
(s/def :data/description string?)
(s/def :data/image #(instance? java.io.File %))
(def base64-regex #"^[A-Za-z0-9_-]+$")
(s/def :data/url #(re-matches base64-regex %))
(s/def :data/sent boolean?)
(s/def :data/image-json (s/keys :req-un [:data/type :data/url]
                                :opt-un [:data/description]))
(s/def :data/image-query (s/keys :req-un [(or :data/type :data/sent)]))

(comment 
  (defn mock-data []
    (doseq [month (range 1 3) day (range 1 28) hour (range 0 24) minute (range 0 60)]
      (when (= 1 (rand-int 2))
        (db/insert-access-timestamp
               (gen/generate (s/gen :data/person_type))
               (gen/generate (s/gen :data/access_type))
               (java.time.LocalDateTime/of 2024 month day hour minute minute))))))

(defn index []
  (res/response (str "Hello")))

(defn access []
  (res/response (db/select-all-access)))

(defn access-last []
  (res/response (db/select-last-access)))

(defn access-year [year]
  (let [startDate (format "%d-00-00" year)
        endDate (format "%d-12-31" year)]

    (res/response (db/select-range-access startDate endDate))))

(defn access-month [year month]
  (let [startDate (format "%d-%02d-00" year month)
        endDate (format "%d-%02d-31" year month)]

    (res/response (db/select-range-access startDate endDate))))

(defn access-week [year month week]
  (let [startDate (format "%d-%02d-%02d" year month (* (- week 1) 7))
        endDate (format "%d-%02d-%02d" year month (* week 7))]

    (res/response (db/select-range-access startDate endDate))))

(defn access-date [year month day]
  (res/response (db/select-date-access (format "%d-%02d-%02d" year month day))))

(defn access-insert [person_type access_type]
  (try
    (let [person (s/conform :data/person_type person_type)
          access (s/conform :data/access_type access_type)]
      (when (s/invalid? person) (throw (IllegalArgumentException. (s/explain-str :data/person_type person_type))))
      (when (s/invalid? access) (throw (IllegalArgumentException. (s/explain-str :data/access_type access_type))))
      
      (db/insert-access person access)
      (res/status (access-last) 201))
    
    (catch IllegalArgumentException e
      (-> (.getMessage e)
          res/response
          (res/status 400)))))

(defn images []
  (res/response (db/select-all-images)))

(defn images-last []
  (res/response (db/select-last-image)))

(defn images-pending []
  (res/response (db/select-pending-images)))

(defn images-some [type sent]
  (res/response (db/select-some-images type sent)))


(def PATTERN_FORMAT "uuuu_MM_dd_HH_mm_ss_A")

(def formatter (.withZone (DateTimeFormatter/ofPattern PATTERN_FORMAT) java.time.ZoneOffset/UTC))

(defn images-insert
  [type image description]

  (try
    (let [parsed-type (s/conform :data/type type)
          parsed-image (s/conform :data/image image)
          parsed-description (s/conform :data/description description)
          url (format "%s.png" (.format formatter (java.time.Instant/now)))
          image-location (io/file (format "%s/%s" "images" url))]
      (when (s/invalid? parsed-type) (throw (IllegalArgumentException. (s/explain-str :data/type type))))
      (when (s/invalid? parsed-image) (throw (IllegalArgumentException. (s/explain-str :data/image image))))
      (when (s/invalid? parsed-description) (throw (IllegalArgumentException. (s/explain-str :data/description description))))

      (io/copy image image-location)

      (db/insert-image type url description)
      (res/status (images-last) 201))
    
    (catch IllegalArgumentException e
      (-> (.getMessage e)
          res/response
          (res/status 400)))))
  
