(ns ui.core
  (:require [reagent.core :as reagent]
            [ui.config :as config]
            [ui.routes :as routes]
            [ui.views :as views]
            [ui.util :as util]
            [ui.events]
            [ui.effects]
            [ui.subs]
            [re-frame.core :as re-frame]))


(defn- dev-setup []
  (when config/debug?
    (enable-console-print!)
    (util/log "dev mode")))


(defn- mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))


(defonce event-listeners
  (fn []
    (letfn [(on-key-down [event]
              (re-frame/dispatch-sync [:key-pressed (util/code->key (.-which event))]))
            (on-key-up [event]
              (re-frame/dispatch-sync [:no-key-pressed]))]
      (.addEventListener js/document "keydown" on-key-down)
      (.addEventListener js/document "keyup" on-key-up))))


(defn ^:export init! []
  (dev-setup)
  (routes/init)
  (event-listeners)
  (mount-root))
