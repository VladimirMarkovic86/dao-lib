(ns dao-lib.core
  (:require [mongo-lib.core :as mon]
            [utils-lib.core :as utils]
            [ajax-lib.http.entity-header :as eh]
            [ajax-lib.http.mime-type :as mt]
            [ajax-lib.http.status-code :as stc]))

(defn build-projection
  "Build projection for db interaction"
  [vector-fields
   include]
  (let [projection (atom {})
        include (if include
                  true
                  false)]
    (doseq [field vector-fields]
      (swap!
        projection
        assoc
        field
        include))
    @projection))

(defn get-entities
  "Prepare data for table"
  [request]
  (try
    (let [request-body (:body
                         request)]
      (if (:pagination request-body)
        (let [current-page (:current-page request-body)
              rows (:rows request-body)
              count-entities (mon/mongodb-count
                               (:entity-type request-body)
                               (:entity-filter request-body))
              number-of-pages (when (:pagination request-body)
                                (utils/round-up
                                  count-entities
                                  rows))
              current-page (if (= current-page
                                  number-of-pages)
                             (dec
                               current-page)
                             current-page)
              entity-type (:entity-type request-body)
              entity-filter (:entity-filter request-body)
              projection-vector (:projection request-body)
              projection-include (:projection-include request-body)
              projection (build-projection
                           projection-vector
                           projection-include)
              qsort (:qsort request-body)
              collation (:collation request-body)
              db-result (mon/mongodb-find
                          entity-type
                          entity-filter
                          projection
                          qsort
                          rows
                          (* current-page
                             rows)
                          collation)]
          {:status (stc/ok)
           :headers {(eh/content-type) (mt/text-clojurescript)}
           :body {:status "success"
                  :data db-result
                  :pagination {:current-page     current-page
                               :rows             rows
                               :total-row-count  count-entities}}
           })
        (let [entity-type (:entity-type request-body)
              entity-filter (:entity-filter request-body)
              projection-vector (:projection request-body)
              projection-include (:projection-include request-body)
              projection (build-projection
                           projection-vector
                           projection-include)
              qsort (:qsort request-body)
              collation (:collation request-body)
              db-result (mon/mongodb-find
                          entity-type
                          entity-filter
                          projection
                          qsort
                          0
                          0
                          collation)]
          {:status (stc/ok)
           :headers {(eh/content-type) (mt/text-clojurescript)}
           :body {:status "success"
                  :data db-result}}))
     )
    (catch Exception ex
      (println (.getMessage ex))
      {:status (stc/internal-server-error)
       :headers {(eh/content-type) (mt/text-clojurescript)}
       :body {:status "Error"
              :status-code 70
              :message (.getMessage ex)}})
   ))

(defn get-entity
  "Prepare requested entity for response"
  [request]
  (try
    (let [request-body (:body
                         request)
          {entity-type :entity-type
           entity-filter :entity-filter
           entity-projection :entity-projection
           projection-include :projection-include} request-body
          entity (mon/mongodb-find-by-id
                   entity-type
                   (:_id entity-filter)
                   (build-projection
                     entity-projection
                     true))
          entity  (assoc
                    entity
                    :_id
                    (str
                      (:_id entity))
                   )]
      (if entity
        {:status (stc/ok)
         :headers {(eh/content-type) (mt/text-clojurescript)}
         :body {:status "success"
                :data entity}}
        {:status (stc/not-found)
         :headers {(eh/content-type) (mt/text-clojurescript)}
         :body {:status "error"
                :error-message "There is no entity, for given criteria."}}))
    (catch Exception e
      (println (.getMessage e))
      {:status (stc/internal-server-error)
       :headers {(eh/content-type) (mt/text-clojurescript)}
       :body {:status "Error"}}))
 )

(defn update-entity
  "Update entity"
  [request]
  (try
    (let [request-body (:body
                         request)]
      (mon/mongodb-update-by-id
        (:entity-type request-body)
        (:_id request-body)
        (:entity request-body))
      {:status (stc/ok)
       :headers {(eh/content-type) (mt/text-clojurescript)}
       :body {:status "success"}})
    (catch com.mongodb.MongoWriteException mex
      (println (.getMessage mex))
      (if (= com.mongodb.ErrorCategory/DUPLICATE_KEY
             (.getCategory
               (.getError
                 mex))
           )
        {:status (stc/internal-server-error)
         :headers {(eh/content-type) (mt/text-clojurescript)}
         :body {:status "Error"
                :message (.getMessage
                           mex)
                :status-code 70
                :message-code 71}}
        {:status (stc/internal-server-error)
         :headers {(eh/content-type) (mt/text-clojurescript)}
         :body {:status "Error"
                :status-code 70
                :message (.getMessage
                           mex)}})
     )
    (catch Exception ex
      (println (.getMessage ex))
      {:status (stc/internal-server-error)
       :headers {(eh/content-type) (mt/text-clojurescript)}
       :body {:status "Error"
              :status-code 70
              :message (.getMessage
                         ex)}})
   ))

(defn insert-entity
  "Insert entity"
  [request]
  (try
    (let [request-body (:body
                         request)]
      (mon/mongodb-insert-one
        (:entity-type request-body)
        (:entity request-body))
      {:status (stc/ok)
       :headers {(eh/content-type) (mt/text-clojurescript)}
       :body {:status "Success"}})
    (catch com.mongodb.MongoWriteException mex
      (println (.getMessage mex))
      (if (= com.mongodb.ErrorCategory/DUPLICATE_KEY
             (.getCategory
               (.getError
                 mex))
           )
        {:status (stc/internal-server-error)
         :headers {(eh/content-type) (mt/text-clojurescript)}
         :body {:status "Error"
                :message (.getMessage mex)
                :status-code 70
                :message-code 71}}
        {:status (stc/internal-server-error)
         :headers {(eh/content-type) (mt/text-clojurescript)}
         :body {:status "Error"
                :status-code 70
                :message (.getMessage
                           mex)}})
     )
    (catch Exception ex
      (println (.getMessage ex))
      {:status (stc/internal-server-error)
       :headers {(eh/content-type) (mt/text-clojurescript)}
       :body {:status "Error"
              :status-code 70
              :message (.getMessage
                         ex)}})
   ))

(defn delete-entity
  "Delete entity"
  [request]
  (try
    (let [request-body (:body
                         request)]
      (mon/mongodb-delete-by-id
        (:entity-type request-body)
        (:_id (:entity-filter request-body))
       )
      {:status (stc/ok)
       :headers {(eh/content-type) (mt/text-clojurescript)}
       :body {:status "success"}})
    (catch Exception ex
      (println (.getMessage ex))
      {:status (stc/internal-server-error)
       :headers {(eh/content-type) (mt/text-clojurescript)}
       :body {:status "Error"
              :status-code 70
              :message (.getMessage
                         ex)}})
   ))

