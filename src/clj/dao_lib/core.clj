(ns dao-lib.core
  (:require [db-lib.core :as db]
            [utils-lib.core :as utils]
            [ajax-lib.http.entity-header :as eh]
            [ajax-lib.http.mime-type :as mt]
            [ajax-lib.http.status-code :as stc]))

(defn get-entities
  "Prepare data for table"
  [request-body]
  (try
    (if (:pagination request-body)
      (let [current-page (:current-page request-body)
            rows (:rows request-body)
            count-entities (db/count-by-filter
                             (:entity-type request-body)
                             (:entity-filter request-body))
            number-of-pages (when (:pagination request-body)
                              (utils/round-up
                                count-entities
                                rows))
            current-page (if (and (= current-page
                                     number-of-pages)
                                  (not= current-page
                                        0))
                           (dec
                             current-page)
                           current-page)
            entity-type (:entity-type request-body)
            entity-filter (:entity-filter request-body)
            projection-vector (:projection request-body)
            projection-include (:projection-include request-body)
            qsort (:qsort request-body)
            collation (:collation request-body)
            db-result (db/find-by-filter
                        entity-type
                        entity-filter
                        projection-vector
                        qsort
                        rows
                        (* current-page
                           rows)
                        collation)]
        {:status (stc/ok)
         :headers {(eh/content-type) (mt/text-plain)}
         :body (str {:status "success"
                     :data db-result
                     :pagination {:current-page     current-page
                                  :rows             rows
                                  :total-row-count  count-entities}})
         })
      (let [entity-type (:entity-type request-body)
            entity-filter (:entity-filter request-body)
            projection-vector (:projection request-body)
            projection-include (:projection-include request-body)
            qsort (:qsort request-body)
            collation (:collation request-body)
            db-result (db/find-by-filter
                        entity-type
                        entity-filter
                        projection-vector
                        qsort
                        0
                        0
                        collation)]
        {:status (stc/ok)
         :headers {(eh/content-type) (mt/text-plain)}
         :body (str {:status "success"
                     :data db-result})})
     )
    (catch Exception ex
      (println (.getMessage ex))
      {:status (stc/internal-server-error)
       :headers {(eh/content-type) (mt/text-plain)}
       :body (str
               {:status "Error"
                :message (.getMessage ex)})}
     ))
 )

(defn get-entity
  "Prepare requested entity for response"
  [request-body]
  (let [entity (db/find-by-id
                 (:entity-type request-body)
                 (:_id (:entity-filter request-body))
                )
        entity  (assoc
                  entity
                  :_id
                  (str
                    (:_id entity))
                 )]
    (if entity
      {:status (stc/ok)
       :headers {(eh/content-type) (mt/text-plain)}
       :body (str {:status  "success"
                   :data  entity})}
      {:status (stc/not-found)
       :headers {(eh/content-type) (mt/text-plain)}
       :body (str {:status  "error"
                   :error-message "There is no entity, for given criteria."})})
   ))

(defn update-entity
  "Update entity"
  [request-body]
  (try
    (db/update-by-id
      (:entity-type request-body)
      (read-string
        (:_id request-body))
      (:entity request-body))
    {:status (stc/ok)
     :headers {(eh/content-type) (mt/text-plain)}
     :body (str {:status "success"})}
    (catch Exception ex
     (println (.getMessage ex))
     {:status (stc/internal-server-error)
      :headers {(eh/content-type) (mt/text-plain)}
      :body (str {:status "error"})})
   ))

(defn insert-entity
  "Insert entity"
  [request-body]
  (try
    (db/insert-one
      (:entity-type request-body)
      (:entity request-body))
    {:status (stc/ok)
     :headers {(eh/content-type) (mt/text-plain)}
     :body (str {:status "success"})}
    (catch Exception ex
     (println (.getMessage ex))
     {:status (stc/internal-server-error)
      :headers {(eh/content-type) (mt/text-plain)}
      :body (str {:status "error"})})
   ))

(defn delete-entity
  "Delete entity"
  [request-body]
  (try
    (db/delete-by-id
      (:entity-type request-body)
      (:_id (:entity-filter request-body))
     )
    {:status (stc/ok)
     :headers {(eh/content-type) (mt/text-plain)}
     :body (str {:status "success"})}
    (catch Exception ex
     (println (.getMessage ex))
     {:status (stc/internal-server-error)
      :headers {(eh/content-type) (mt/text-plain)}
      :body (str {:status "error"})})
   ))

