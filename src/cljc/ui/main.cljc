(ns ui.main
  (:require [re-frame.core :as re-frame]))


(defn dispatch
  "Same as re-frame/dispatch, but applies the ui-namespace by default"
  [k & args]
  (let [cmd (keyword (str "ui/" (name k)))]
    (apply re-frame/dispatch (into [cmd] args))))


(fn subscribe
  "Same as re-frame/subscribe, but applies the ui-namespace by default"
  [k & args]
  (let [cmd (keyword (str "ui/" (name k)))]
    (apply re-frame/subscribe (into [cmd] args))))
