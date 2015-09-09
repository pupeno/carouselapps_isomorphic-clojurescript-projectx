(ns projectx.handler
  (:require [clojure.java.io :as io]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]
            [aleph.flow :as flow])
  (:import [javax.script ScriptEngineManager
                         Invocable]
           [io.aleph.dirigiste Pools]))

(defn- create-js-engine []
  (doto (.getEngineByName (ScriptEngineManager.) "nashorn")
    (.eval "var global = this")                             ; React requires either "window" or "global" to be defined.
    (.eval (-> "public/js/server-side.js"                   ; TODO: load the console polyfill, so that calling console.log is safe.
               io/resource
               io/reader))))

(def ^:private js-engine-key "js-engine")                   ; We have one and only one key in the pool, it's a constant.
(def ^:private js-engine-pool
  (flow/instrumented-pool
    {:generate   (fn [_] (create-js-engine))
     :controller (Pools/utilizationController 0.9 10000 10000)}))

(defn- render-page [path]
  (let [js-engine @(flow/acquire js-engine-pool js-engine-key)]
    (try (.invokeMethod
           ^Invocable js-engine
           (.eval js-engine "projectx.core")
           "render_page"
           (object-array [path]))
         (finally (flow/release js-engine-pool js-engine-key js-engine)))))

(defn render-app [path]
  (html
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name    "viewport"
              :content "width=device-width, initial-scale=1"}]
      (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
     [:body
      [:div#app [:div (render-page path)]]
      (include-js "js/app.js")]]))

(defn- path [request]
  (str (:uri request)                                       ; Build the path the same way ring.util.request/request-url does it: https://github.com/ring-clojure/ring/blob/1.4.0/ring-core/src/ring/util/request.clj#L5
       (if-let [query (:query-string request)]
         (str "?" query))))

(defroutes routes
  (GET "*" request (render-app (path request)))
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults #'routes site-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))
