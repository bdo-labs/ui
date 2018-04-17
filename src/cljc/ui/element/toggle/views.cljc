(ns ui.element.toggle.views
  (:require [ui.element.checkbox.views :refer [checkbox]]))

(defn toggle [params label]
  [checkbox (merge params {:class [:Toggle]}) label])
