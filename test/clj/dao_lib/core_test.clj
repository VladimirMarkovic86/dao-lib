(ns dao-lib.core-test
  (:require [clojure.test :refer :all]
            [dao-lib.core :refer :all]
            [mongo-lib.core :as mon]
            [ajax-lib.http.entity-header :as eh]
            [ajax-lib.http.mime-type :as mt]
            [ajax-lib.http.status-code :as stc]
            [session-lib.core :as ssn]))

(def db-uri
     (or (System/getenv "MONGODB_URI")
         (System/getenv "PROD_MONGODB")
         "mongodb://admin:passw0rd@127.0.0.1:27017/admin"))

(def db-name
     "test-db")

(defn create-db
  "Create database for testing"
  []
  (mon/mongodb-connect
    db-uri
    db-name)
  (ssn/create-indexes)
  (mon/mongodb-insert-many
    "user"
    [{:username "test-username-1"
      :email "test-email-1"
      :password "test-password-1"}
     {:username "test-username-2"
      :email "test-email-2"
      :password "test-password-2"}])
  )

(defn destroy-db
  "Destroy testing database"
  []
  (mon/mongodb-drop-collection
    "user")
  (mon/mongodb-disconnect))

(defn before-and-after-tests
  "Before and after tests"
  [f]
  (create-db)
  (f)
  (destroy-db))

(use-fixtures :once before-and-after-tests)

(deftest test-build-projection
  
  (testing "Test build projection"
    
    (is
      (= (build-projection
           []
           true)
         {})
     )
    
    (is
      (= (build-projection
           []
           false)
         {})
     )
    
    (is
      (= (build-projection
           []
           nil)
         {})
     )
    
    (is
      (= (build-projection
           [:test-key-1
            :test-key-2]
           nil)
         {:test-key-1 false
          :test-key-2 false})
     )
    
    (is
      (= (build-projection
           [:test-key-1
            :test-key-2]
           1)
         {:test-key-1 true
          :test-key-2 true})
     )
    
    (is
      (= (build-projection
           [:test-key-1
            :test-key-2]
           true)
         {:test-key-1 true
          :test-key-2 true})
     )
    
    (is
      (= (build-projection
           [:test-key-1
            :test-key-2]
           false)
         {:test-key-1 false
          :test-key-2 false})
     )
    
   )
  
 )

(deftest test-get-entities
  
  (testing "Test get entities"
    
    (let [request {}
          response (get-entities
                     request)]
      
      (is
        (= (:status response)
           (stc/ok))
       )
      
      (is
        (= (get-in
             response
             [:headers
              (eh/content-type)])
           (mt/text-clojurescript))
       )
      
      (is
        (= (get-in
             response
             [:body
              :status])
           "success")
       )
      
     )
    
    (let [request {:body {:entity-type "user"
                          :entity-filter {:username "not-existing-username"}}
                   }
          response (get-entities
                     request)]
      
      (is
        (= response
           {:status (stc/ok)
            :headers {(eh/content-type) (mt/text-clojurescript)}
            :body {:status "success"
                   :data []}})
       )
      
     )
    
    (let [request {:body {:entity-type "user"
                          :projection [:_id]
                          :projection-include false}}
          response (get-entities
                     request)]
      
      (is
        (= response
           {:status (stc/ok)
            :headers {(eh/content-type) (mt/text-clojurescript)}
            :body {:status "success"
                   :data [{:username "test-username-1"
                           :email "test-email-1"
                           :password "test-password-1"}
                          {:username "test-username-2"
                           :email "test-email-2"
                           :password "test-password-2"}]}})
       )
      
     )
    
   )
  
 )

(deftest test-get-entity
  
  (testing "Test get entity"
    
    (let [request {}
          response (get-entity
                     request)]
      
      (is
        (= response
           {:status (stc/ok)
            :headers {(eh/content-type) (mt/text-clojurescript)}
            :body {:status "success"
                   :data {:_id ""}}
            })
       )
      
     )
    
    (let [request {:body {:entity-type "user"
                          :entity-filter {:_id ""}}}
          response (get-entity
                     request)]
      
      (is
        (= response
           {:status (stc/ok)
            :headers {(eh/content-type) (mt/text-clojurescript)}
            :body {:status "success"
                   :data {:_id ""}}
            })
       )
       
     )
    
    (is
      (mon/mongodb-exists
        "user"
        {:username "test-username-1"})
     )
    
    (let [test-user-from-db (mon/mongodb-find-one
                              "user"
                              {:username "test-username-1"})
          request {:body {:entity-type "user"
                          :entity-filter {:_id (:_id test-user-from-db)}}
                   }
          response (get-entity
                     request)]
      
      (is
        (= (:status response)
           (stc/ok))
       )
      
      (is
        (= (get-in
             response
             [:headers
              (eh/content-type)])
           (mt/text-clojurescript))
       )
      
      (is
        (= (get-in
             response
             [:body
              :status])
           "success")
       )
      
      (is
        (= (get-in
             response
             [:body
              :data
              :username])
           "test-username-1")
       )
      
      (is
        (= (get-in
             response
             [:body
              :data
              :email])
           "test-email-1")
       )
      
      (is
        (= (get-in
             response
             [:body
              :data
              :password])
           "test-password-1")
       )
      
     )
    
   )
  
 )

(deftest test-update-entity
  
  (testing "Test update entity"
    
    (let [request {}
          response (update-entity
                     request)]
      
      (is
        (= (:status response)
           (stc/ok))
       )
      
      (is
        (= (get-in
             response
             [:headers
              (eh/content-type)])
           (mt/text-clojurescript))
       )
      
      (is
        (= (get-in
             response
             [:body
              :status])
           "success")
       )
      
     )
    
    (let [request {:entity-type "user"
                   :_id ""
                   :entity {}}
          response (update-entity
                     request)]
      
      (is
        (= (:status response)
           (stc/ok))
       )
      
      (is
        (= (get-in
             response
             [:headers
              (eh/content-type)])
           (mt/text-clojurescript))
       )
      
      (is
        (= (get-in
             response
             [:body
              :status])
           "success")
       )
      
     )
    
    (is
      (mon/mongodb-exists
        "user"
        {:username "test-username-1"})
     )
    
    (let [test-user-from-db (mon/mongodb-find-one
                              "user"
                              {:username "test-username-1"})
          request {:body {:entity-type "user"
                          :_id (:_id test-user-from-db)
                          :entity {:email "test-email-1-changed"}}
                   }
          response (update-entity
                     request)]
      
      (is
        (= response
           {:status (stc/ok)
            :headers {(eh/content-type) (mt/text-clojurescript)}
            :body {:status "success"}})
       )
      
      (is
        (mon/mongodb-exists
          "user"
          {:username "test-username-1"
           :email "test-email-1-changed"
           :password "test-password-1"})
       )
      
     )
    
    (let [test-user-from-db (mon/mongodb-find-one
                              "user"
                              {:username "test-username-1"})
          request {:body {:entity-type "user"
                          :_id (:_id test-user-from-db)
                          :entity {:email "test-email-2"}}
                   }
          response (update-entity
                     request)]
      
      (is
        (= (:status response)
           (stc/internal-server-error))
       )
      
      (is
        (= (get-in
             response
             [:headers
              (eh/content-type)])
           (mt/text-clojurescript))
       )
      
      (is
        (= (get-in
             response
             [:body
              :status])
           "Error")
       )
      
      (is
        (= (get-in
             response
             [:body
              :status-code])
           70)
       )
      
     )
    
    (let [test-user-from-db (mon/mongodb-find-one
                              "user"
                              {:username "test-username-1"})
          request {:body {:entity-type "user"
                          :_id (:_id test-user-from-db)
                          :entity {:username "test-username-2"}}
                   }
          response (update-entity
                     request)]
      
      (is
        (= (:status response)
           (stc/internal-server-error))
       )
      
      (is
        (= (get-in
             response
             [:headers
              (eh/content-type)])
           (mt/text-clojurescript))
       )
      
      (is
        (= (get-in
             response
             [:body
              :status])
           "Error")
       )
      
      (is
        (= (get-in
             response
             [:body
              :status-code])
           70)
       )
      
     )
    
   )
 )


(deftest test-insert-entity
  
  (testing "Test insert entity"
    
    (let [request {}
          response (insert-entity
                     request)]
      
      (is
        (= (:status response)
           (stc/ok))
       )
      
      (is
        (= (get-in
             response
             [:headers
              (eh/content-type)])
           (mt/text-clojurescript))
       )
      
      (is
        (= (get-in
             response
             [:body
              :status])
           "Success")
       )
      
     )
    
    (let [request {:entity-type "user"
                   :entity {}}
          response (insert-entity
                     request)]
      
      (is
        (= (:status response)
           (stc/ok))
       )
      
      (is
        (= (get-in
             response
             [:headers
              (eh/content-type)])
           (mt/text-clojurescript))
       )
      
      (is
        (= (get-in
             response
             [:body
              :status])
           "Success")
       )
      
     )
    
    (is
      (not
        (mon/mongodb-exists
          "user"
          {:username "test-username-4"}))
     )
    
    (let [request {:body {:entity-type "user"
                          :entity {:username "test-email-4"
                                   :email "test-email-4"
                                   :password "test-password-4"}}
                   }
          response (insert-entity
                     request)]
      
      (is
        (= response
           {:status (stc/ok)
            :headers {(eh/content-type) (mt/text-clojurescript)}
            :body {:status "Success"}})
       )
      
      (is
        (mon/mongodb-exists
          "user"
          {:username "test-email-4"
           :email "test-email-4"
           :password "test-password-4"})
       )
      
      (mon/mongodb-delete-one
        "user"
        {:username "test-email-4"
         :email "test-email-4"
         :password "test-password-4"})
      
      (is
        (not
          (mon/mongodb-exists
            "user"
            {:username "test-email-4"
             :email "test-email-4"
             :password "test-password-4"}))
       )
      
     )
    
    (is
      (mon/mongodb-exists
        "user"
        {:username "test-username-1"})
     )
    
    (let [request {:body {:entity-type "user"
                          :entity {:username "test-username-1"
                                   :email "test-email-4"
                                   :password "test-password-4"}}
                   }
          response (insert-entity
                     request)]
      
      (is
        (= (:status response)
           (stc/internal-server-error))
       )
      
      (is
        (= (get-in
             response
             [:headers
              (eh/content-type)])
           (mt/text-clojurescript))
       )
      
      (is
        (= (get-in
             response
             [:body
              :status])
           "Error")
       )
      
      (is
        (= (get-in
             response
             [:body
              :status-code])
           70)
       )
      
     )
    
    (is
      (mon/mongodb-exists
        "user"
        {:email "test-email-1"})
     )
    
    (let [request {:body {:entity-type "user"
                          :entity {:username "test-username-4"
                                   :email "test-email-1"
                                   :password "test-password-4"}}
                   }
          response (insert-entity
                     request)]
      
      (is
        (= (:status response)
           (stc/internal-server-error))
       )
      
      (is
        (= (get-in
             response
             [:headers
              (eh/content-type)])
           (mt/text-clojurescript))
       )
      
      (is
        (= (get-in
             response
             [:body
              :status])
           "Error")
       )
      
      (is
        (= (get-in
             response
             [:body
              :status-code])
           70)
       )
      
     )
    
   )
  
 )

(deftest test-delete-entity
  
  (testing "Test delete entity"
    
    (let [request {}
          response (delete-entity
                     request)]
      
      (is
        (= (:status response)
           (stc/ok))
       )
      
      (is
        (= (get-in
             response
             [:headers
              (eh/content-type)])
           (mt/text-clojurescript))
       )
      
      (is
        (= (get-in
             response
             [:body
              :status])
           "success")
       )
      
     )
    
    (let [request {:body
                    {:entity-type "user"
                     :entity-filter {:_id ""}}
                   }
          response (delete-entity
                     request)]
      
      (is
        (= (:status response)
           (stc/internal-server-error))
       )
      
      (is
        (= (get-in
             response
             [:headers
              (eh/content-type)])
           (mt/text-clojurescript))
       )
      
      (is
        (= (get-in
             response
             [:body
              :status])
           "Error")
       )
      
      (is
        (= (get-in
             response
             [:body
              :status-code])
           70)
       )
      
     )
    
    (insert-entity
      {:body {:entity-type "user"
              :entity {:username "test-username-4"
                       :email "test-email-4"
                       :password "test-password-4"}}
       })
    
    (is
      (mon/mongodb-exists
        "user"
        {:username "test-username-4"
         :email "test-email-4"
         :password "test-password-4"})
     )
    
    (let [test-user-from-db (mon/mongodb-find-one
                              "user"
                              {:username "test-username-4"
                               :email "test-email-4"
                               :password "test-password-4"})
          request {:body
                    {:entity-type "user"
                     :entity-filter {:_id (:_id test-user-from-db)}}
                   }
          response (delete-entity
                     request)]
      
      (is
        (= response
           {:status (stc/ok)
            :headers {(eh/content-type) (mt/text-clojurescript)}
            :body {:status "success"}})
       )
      
     )
    
    (is
      (not
        (mon/mongodb-exists
          "user"
          {:username "test-username-4"
           :email "test-email-4"
           :password "test-password-4"}))
     )
    
   )
  
 )

