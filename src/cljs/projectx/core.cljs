(ns projectx.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [bidi.bidi :as bidi]
              [pushy.core :as pushy])
    (:import goog.History))

;; -------------------------
;; Routes
(def routes ["/" {""      :home-page
                  "about" :about-page}])

;; -------------------------
;; Views
(defn home-page []
  [:div [:h2 "Welcome to projectx"]
   [:div [:a {:href (bidi/path-for routes :about-page)} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About projectx"]
   [:div [:a {:href (bidi/path-for routes :home-page)} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])


;; -------------------------
;; Routing and wiring
(defn parse-path [path]
  (case (:handler (bidi/match-route routes path))
    :home-page #'home-page
    :about-page #'about-page
    (throw (js/Error. (str "Path not recognized: " (pr-str path))))))

(defn set-current-page [parsed-path]
  (session/put! :current-page parsed-path))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (pushy/start! (pushy/pushy set-current-page parse-path))
  (mount-root))
