(ns ui.main
  (:require [re-frame.core :as re-frame]))


#_(defn add-boundaries []
  (let [boundaries @(re-frame/subscribe [:boundaries])]
    ))


#_(defn init []
  (add-boundaries))
