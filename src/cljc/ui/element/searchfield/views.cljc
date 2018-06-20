(ns ui.element.searchfield.views
  (:require [ui.element.textfield.views :refer [textfield]]))

(defn searchfield [params]
  [textfield (merge {:type :search :class "Search"} params)])
