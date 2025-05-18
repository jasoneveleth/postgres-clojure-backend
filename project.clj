(defproject backend "0.1.0-SNAPSHOT"
  :description "Server for managing undo tree operations and state"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.9.6"]
                 [ring/ring-jetty-adapter "1.9.6"]
                 [compojure "1.7.0"]
                 [ring/ring-json "0.5.1"]
                 [org.clojure/data.json "2.4.0"]
                 [com.github.seancorfield/next.jdbc "1.3.883"]
                 [org.clojure/tools.logging "1.2.4"]
                 [http-kit/http-kit "2.8.0"]
                 [ch.qos.logback/logback-classic "1.4.8"] ; SLF4J implementation
                 [org.postgresql/postgresql "42.5.4"] ; For PostgreSQL
                 [com.zaxxer/HikariCP "5.0.1"]        ; Connection pooling
                 [metosin/muuntaja "0.6.8"]          ; Content negotiation
                 [ring-cors "0.1.13"]]               ; CORS support
  :main ^:skip-aot backend.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
