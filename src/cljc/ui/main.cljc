(ns ui.main
  (:require [re-frame.core :as re-frame]))


(defn dispatch
  "Same as re-frame/dispatch but applies the ui-namespace by default"
  [k & args]
  (let [cmd (keyword (str "ui/" (name k)))]
    (apply re-frame/dispatch (into [cmd] args))))


#_(defn add-boundaries []
  (let [boundaries @(re-frame/subscribe [:boundaries])]
    ))


#_(defn init []
  (add-boundaries))
