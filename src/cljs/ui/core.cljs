(ns ui.core
  (:require [reagent.core :as reagent]
            [weasel.repl :as repl]
            [ui.config :as config]
            [ui.routes :as routes]
            [ui.views :as views]
            [ui.util :as util]
            [ui.events]
            [ui.effects]
            [ui.subs]
            [ui.wire.polyglot]))


(defn- dev-setup []
  (when config/debug?
    (enable-console-print!)
    (util/log "dev mode")
    ;; try to set up a Weasel REPL
    (try
      (util/log "Setting up Weasel REPL")
      (repl/connect "ws://localhost:9001")
      (catch js/Error _
        (util/log "Unable to connect to the Weasel REPL")))))

(defn- mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))


(defn ^:export init! []
  (dev-setup)
  (routes/init)
  (mount-root))
