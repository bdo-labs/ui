(ns ui.docs.collection
  (:require [ui.element.collection.spec :as spec]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.element.showcase.views :refer [showcase]]))

(defn documentation []
  [layout/vertically {:raised? true :background :white :rounded? true
                      :style   {:margin  "2em"
                                :padding "3em"
                                :flex    "1 1 80%"}}
   [showcase #'ui.element.collection.views/collection ::spec/args]])
