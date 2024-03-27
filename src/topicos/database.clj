(ns topicos.database
  (:require
   [clojure.tools.logging :as logging]
   [clojure.spec.alpha :as s]
   [topicos.util :as util]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :refer [as-unqualified-maps]]
   [honey.sql :as sql]))

(def db {:dbtype "sqlite"
         :dbname "database"})

(def ds (jdbc/get-datasource db))

(defmacro execute-sql [query]
  `(logging/info ~query)
  `(jdbc/execute! ds
                  (sql/format ~query) {:builder-fn as-unqualified-maps}))

(defn select-all-metrics []
  (execute-sql {:select [:humidity :time]
                              :from [:metrics]}))

(defn select-last-metric []
  (execute-sql {:select [:humidity :time]
                              :from [:metrics]
                              :order-by [[:id :desc]]
                              :limit 1}))

(defn insert-metric [humidity]
  (let [timestamp (java.time.LocalDateTime/ofInstant (java.time.Instant/now) (java.time.ZoneId/of "Mexico/General"))]
    (execute-sql {:insert-into [:metrics]
                                  :columns [:humidity :time]
                  :values [[humidity timestamp]]})))

(defn select-all-images []
  (execute-sql {:select [:type :description :sent]
                :from [:images]}))


;; Black Magic, should find cleaner version
(defn- select-some-images-where [type sent]
  (into []
        (remove nil? [(if (not (nil? type)) [:like :type type] nil)
                      (if (not (nil? sent)) [:= :sent sent] nil)])))

(defn select-some-images [type sent]
  (execute-sql {:select [:type :url :description :sent]
                :from [:images]
                :where (select-some-images-where type sent)}))

(defn select-pending-images []
  (let [return-value (execute-sql {:select [:type :url :description :sent]
                :from [:images]
                                   :where [[:= :sent :false]]})]
    (execute-sql {:update :images
                  :set {:sent :true}
                  :where [[:= :sent :false]]})

    return-value))

(defn select-last-image []
  (execute-sql {:select [:type :url :description :sent]
                :from [:images]
                :order-by [[:id :desc]]
                :limit 1}))

(defn insert-image [type url description]
  (execute-sql {:insert-into [:images]
                :columns [:type :url :description]
                :values [[type url description]]}))


;; Used by ring init
(defn- create-table
  "Creates both sqlite database tables"
  []
  
  (execute-sql {:create-table [:metrics :if-not-exists]
                                 :with-columns
                                 [[:id :integer :primary-key :autoincrement]
                                  [:humidity :float [:not nil]]
                                  [:time :datetime [:not nil]]]})

  (execute-sql {:create-table [:images :if-not-exists]
                                 :with-columns
                                 [[:id :integer :primary-key :autoincrement]
                                  [:type :text [:not nil]]
                                  [:url :text [:not nil]]
                                  [:description :text]
                                  [:sent :boolean [:default :false][:not nil]]]
                                 }))
