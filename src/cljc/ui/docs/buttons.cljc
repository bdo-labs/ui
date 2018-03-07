(ns ui.docs.buttons
  (:require [ui.element.button :as button]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.element.showcase :refer [showcase]]))


(defn documentation []
  [layout/vertically {:raised? true :background :white :rounded? true
                      :style {:margin "2em"
                              :padding "3em"
                              :flex "1 1 80%"}}
   [showcase #'ui.element.button/button ::button/params]])
