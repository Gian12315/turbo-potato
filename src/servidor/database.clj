(ns servidor.database
  (:require
   [clojure.tools.logging :as logging]
   [clojure.spec.alpha :as s]
   [servidor.util :as util]
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

(defn get-current-time []
  (java.time.LocalDateTime/ofInstant
   (java.time.Instant/now)
   (java.time.ZoneId/of "Mexico/General")))

(defn select-all-access []
  (execute-sql {:select [:person_type :access_type :time]
                :from [:access]}))

(defn select-date-access [date]
  (execute-sql {:select [:person_type :access_type :time]
                :from [:access]
                :where [:= [:date :time] date]}))

(defn select-range-access [startDate endDate]
  (execute-sql {:select [:person_type :access_type :time]
                :from [:access]
                :where [:between :time startDate endDate]}))

(defn select-last-access []
  (execute-sql {:select [:person_type :access_type :time]
                :from [:access]
                :order-by [[:id :desc]]
                :limit 1}))

(defn insert-access [person_type access_type]
  (execute-sql {:insert-into [:access]
                :columns [:person_type :access_type :time]
                :values [[person_type access_type (get-current-time)]]}))

(defn insert-access-timestamp [person_type access_type timestamp]
    (execute-sql {:insert-into [:access]
                  :columns [:person_type :access_type :time]
                  :values [[person_type access_type timestamp]]}))

(defn select-all-images []
  (execute-sql {:select [:type :url :description :sent]
                :from [:images]}))


(defn select-some-images [type sent]
  (execute-sql {:select [:type :url :description :sent]
                :from [:images]
                :where [:and
                        (if (not (nil? type)) [:like :type type] nil)
                        (if (not (nil? sent)) [:= :sent sent] nil)]}))

(defn select-pending-images []
  (let [return-value
        (execute-sql {:select [:type :url :description :sent]
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
                :columns [:type :url :description :time]
                :values [[type url description (get-current-time)]]}))

;; Used by ring init
(defn- create-table
  "Creates both sqlite database tables"
  []
  
  (execute-sql {:create-table [:access :if-not-exists]
                :with-columns [[:id :integer :primary-key :autoincrement]
                               [:access_type :text [:not nil]]
                               [:person_type :text [:not nil]]
                               [:time :datetime [:not nil]]]})

  (execute-sql {:create-table [:images :if-not-exists]
                :with-columns [[:id :integer :primary-key :autoincrement]
                               [:type :text [:not nil]]
                               [:url :text [:not nil]]
                               [:description :text]
                               [:sent :boolean [:default :false][:not nil]]
                               [:time :datetime [:not nil]]]}))



