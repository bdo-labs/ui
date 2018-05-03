(ns ui.element.searchfield.views
  (:require [ui.element.textfield.views :refer [textfield]]))

(defn searchfield [params]
  [textfield (merge params {:type :search :class "Search"})])
