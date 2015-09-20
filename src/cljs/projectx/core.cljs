(ns projectx.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [clojure.set :refer [rename-keys]]
            [domkm.silk :as silk]
            [pushy.core :as pushy])
  (:import goog.History))

;; -------------------------
;; Routes
(def routes (silk/routes [[:home-page [[]]]
                          [:about-page [["about"]]]]))

(defn sanitize-silk-keywords [matched-route]
  (rename-keys matched-route {:domkm.silk/name    :name
                              :domkm.silk/pattern :pattern
                              :domkm.silk/routes  :routes
                              :domkm.silk/url     :url}))

;; -------------------------
;; Views
(defn home-page []
  [:div [:h2 "Welcome to projectx"]
   [:div [:a {:href (silk/depart routes :about-page)} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About projectx"]
   [:div [:a {:href (silk/depart routes :home-page {})} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])


;; -------------------------
;; Routing and wiring
(defn parse-path [path]
  (case (:name (sanitize-silk-keywords (silk/arrive routes path)))
    :home-page #'home-page
    :about-page #'about-page
    (throw (js/Error. (str "Path not recognized: " (pr-str path))))))

(defn set-current-page [parsed-path]
  (session/put! :current-page parsed-path))

(defn ^:export render-page [path]
  (reagent/render-to-string [(parse-path path)]))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (pushy/start! (pushy/pushy set-current-page parse-path))
  (mount-root))
