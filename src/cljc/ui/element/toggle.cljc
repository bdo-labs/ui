(ns ui.element.toggle
  (:require [ui.element.checkbox :refer [checkbox]]))


(defn toggle [params label]
  [checkbox (merge params {:class [:Toggle]}) label])
