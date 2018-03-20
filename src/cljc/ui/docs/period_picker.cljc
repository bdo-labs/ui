(ns ui.docs.period-picker
  (:require [ui.layout :as layout]
            [ui.element.period-picker :as period-picker]
            [ui.element.showcase :refer [showcase]]))

(defn documentation []
  [layout/vertically {:raised? true :background :white :rounded? true
                      :style   {:margin  "2em"
                                :padding "3em"
                                :flex    "1 1 80%"}}
   [showcase #'ui.element.period-picker/period-picker ::period-picker/params]])
