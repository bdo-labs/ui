(ns ui.docs.period-picker
  (:require [ui.layout :as layout]
            [ui.element.period-picker.spec :as period-picker]
            [ui.element.showcase.views :refer [showcase]]))

(defn documentation []
  [layout/vertically {:raised? true :background :white :rounded? true
                      :style   {:margin  "2em"
                                :padding "3em"
                                :flex    "1 1 80%"}}
   [showcase #'ui.element.period-picker.views/period-picker ::period-picker/params]])
