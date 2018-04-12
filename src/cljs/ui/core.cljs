(ns ui.core
  (:require [reagent.core :as reagent]
            [ui.config :as config]
            [ui.routes :as routes]
            [ui.views :as views]
            [ui.util :as util]
            [ui.events]
            [ui.effects]
            [ui.subs]
            [ui.wire.polyglot]
            [ui.virtuals]
            [cljsjs.highlight]
            [cljsjs.highlight.langs.clojure]))

(defn- dev-setup []
  (when config/debug?
    (enable-console-print!)))

(defn- mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init! []
  (dev-setup)
  (routes/init)
  (mount-root))
