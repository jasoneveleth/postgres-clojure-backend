(ns backend.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.codec :as codec]
            [org.httpkit.server :as server]
            [next.jdbc :as jdbc]
            [clojure.data.json :as json])
  (:gen-class))

(def db-spec
  {:dbtype "postgresql"
   :dbname "undotree"
   :host "localhost"
   :port 5432
   :user "jason"
   :password ""
   :maximumPoolSize 10})

(def db (jdbc/get-datasource db-spec))

(defn make-psql-json [dict]
  (doto (org.postgresql.util.PGobject.)
    (.setType "json")
    (.setValue (json/write-str dict))))

(defn create-table []
  (jdbc/execute! db ["CREATE TABLE IF NOT EXISTS undo_tree (
                      node_id SERIAL PRIMARY KEY,
                      parent_id INT NULL,
                      op_type VARCHAR (50) NOT NULL,
                      op_data TEXT NOT NULL,
                      full_state_snapshot JSON NOT NULL,
                      timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      FOREIGN KEY (parent_id) REFERENCES undo_tree (node_id));"])
  (jdbc/execute! db ["INSERT INTO undo_tree (node_id, op_type, op_data, full_state_snapshot)
                      VALUES (?, ?, ?, ?)
                      ON CONFLICT DO NOTHING" 0 "init" "" (make-psql-json {})])
  (jdbc/execute! db ["CREATE INDEX IF NOT EXISTS idx_parent_id ON undo_tree (parent_id);"])
  (jdbc/execute! db ["CREATE INDEX IF NOT EXISTS idx_timestamp ON undo_tree (timestamp);"])
  (jdbc/execute! db ["CREATE TABLE IF NOT EXISTS curr_node_id (
                      ref_id SERIAL PRIMARY KEY,
                      node_id INT NOT NULL,
                      FOREIGN KEY (node_id) REFERENCES undo_tree(node_id));"])
  (jdbc/execute! db ["INSERT INTO curr_node_id (ref_id, node_id) 
                      VALUES (?, ?) 
                      ON CONFLICT DO NOTHING" 1 0]))

(defn name-handler
  [name]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str {"body" (str "Hello world! my name is " name "!")})})

(defn health []
  {:status 200 
   :headers {"Content-Type" "application/json"}
   :body (json/write-str {"version" "1.0.0"
                          "status" "UP"})})

(def unimplemented
  {:status 402
   :headers {"Content-Type" "application/json"}
   :body (json/write-str {"error" "unimplemented"})})

(defn get-current-state []
  (let [{node-id :curr_node_id/node_id} (first (jdbc/execute! db ["select * from curr_node_id"]))
        {pgobj :undo_tree/full_state_snapshot} (first (jdbc/execute! db ["SELECT * from undo_tree WHERE node_id = ?;" node-id]))
        state (json/read-str (.getValue pgobj))]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str {"version" "1.0.0"
                            "ret" state})}))

(defn do-handler [request]
  (let [data (json/read-str (slurp (:body request)))
        {op-type "op_type" op-data "op_data" state "state"} data
        {parent-id :curr_node_id/node_id} (first (jdbc/execute! db ["select * from curr_node_id"]))] 
    (jdbc/execute! db ["INSERT INTO undo_tree (parent_id, op_type, op_data, full_state_snapshot)
                          VALUES (?, ?, ?, ?);" 
                         parent-id op-type (make-psql-json op-data) (make-psql-json state)])
    {:status 201
      :headers {"Content-Type" "application/json"}
      :body (json/write-str {"version" "1.0.0"})}))

(defroutes app-routes
  (GET "/name/:name" [name] (name-handler name))
  (GET "/api_v1/get-health" [] (health))
  (GET "/api_v1/get-undo-tree" [] unimplemented)
  (POST "/api_v1/do" data (do-handler data))
  (GET "/api_v1/redo" [] unimplemented)
  (GET "/api_v1/undo" [] unimplemented)
  (GET "/api_v1/can-redo" [] unimplemented)
  (GET "/api_v1/can-undo" [] unimplemented)
  (GET "/api_v1/get-current-state" [] (get-current-state))
  (GET "/" [] unimplemented))

(defn -main
  "Main Program"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))] 
    (create-table)
    (server/run-server #'app-routes {:port port})
    (println (str "Running the server at http://localhost:" port "/"))))