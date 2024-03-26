(ns topicos.database
  (:require
   [clojure.tools.logging :as logging]
   [clojure.spec.alpha :as s]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :refer [as-unqualified-maps]]
   [honey.sql :as sql]))

(def db {:dbtype "sqlite"
         :dbname "database"})

(def ds (jdbc/get-datasource db))

(defmacro execute-sql [query]
  `(jdbc/execute! ds
                  (sql/format ~query) {:builder-fn as-unqualified-maps}))

(defn select-all-metrics []
  (execute-sql {:select [:humidity :time]
                              :from [:metrics]}))

(defn select-last-metric []
  (jdbc/execute! ds
                 (sql/format {:select [:humidity :time]
                              :from [:metrics]
                              :order-by [[:id :desc]]
                              :limit 1})))

(defn insert-metric [humidity]
  (let [timestamp (java.time.LocalDateTime/ofInstant (java.time.Instant/now) (java.time.ZoneId/of "Mexico/General"))]
    (execute-sql {:insert-into [:metrics]
                                  :columns [:humidity :time]
                  :values [[humidity timestamp]]})))

(defn select-all-images []
  (execute-sql {:select [:type :url]
                :from [:images]}))

(defn select-some-images [type]
  (execute-sql {:select [:type :url]
                :from [:images]
                :where [[:like :type type]]}))

(defn insert-image [register]
  (execute-sql {:insert-into [:images]
                :columns [:type :url]
                :values [[(:type register) (:url register)]]}))


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
                                  [:url :text [:not nil]]]
                                 }))
