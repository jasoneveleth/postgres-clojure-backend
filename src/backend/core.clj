(ns backend.core
  (:require [ring.adapter.jetty :as jetty]
            [compojure.core :refer [defroutes GET POST PUT DELETE]]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.util.response :refer [response status]]
            [org.httpkit.server :as server]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [clojure.data.json :as json])
  (:gen-class))

(def db-spec
  {:dbtype "postgresql"
   :dbname "undo_tree_db"
   :host "localhost"     ; Change as needed
   :port 5432            ; Change as needed
   :user "postgres"      ; Change as needed
   :password "password"  ; Change as needed
   :maximumPoolSize 10})

(defn name-handler
  [name]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str {"body" (str "Hello world! my name is " name "!")})})

(defroutes app-routes
  (GET "/name/:name" [name] (name-handler name)))

(defn -main
  "Main Program"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (server/run-server #'app-routes {:port port})
    (println (str "Running the server at http://localhost:" port "/"))))