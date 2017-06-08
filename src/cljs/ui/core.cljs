(ns ui.core
  (:import goog.History)
  (:require [goog.events :as events]
            [goog.history.EventType :as EventType]
            [re-frame.core :as re-frame]
            [re-frisk.core :refer [enable-re-frisk!]]
            [reagent.core :as reagent]
            [secretary.core :as secretary]
            [devtools.core :as devtools]
            [ui.config :as config]
            [ui.routes :as routes]
            [ui.views :as views]
            [ui.events]
            [ui.subs]))


;; In development-mode we enable Re-frisk, a little panel that keeps
;; track of the entire application-state
(defn dev-setup []
  (when config/debug?
    (devtools/install!)
    (enable-console-print!)
    (enable-re-frisk!)
    (println "dev mode")))


;; We expose Re-frame's `dispatch` in case `ui` is used with a Reagent
;; project.
(defn dispatch
  [& args]
  (apply re-frame/dispatch args))


(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))


(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))


(defn ^:export init []
  (secretary/set-config! :prefix "#")
  (routes/app-routes)
  (hook-browser-navigation!)
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root))
