(ns ui.docs.horizontally
  (:require [ui.elements :as element]
            [ui.layout :as layout]))


(defn documentation
  []
  [element/article
   "# Horizontally"
   [layout/vertically {:fill true}
    [layout/horizontally {:align "left" :class [:demo] :fill true}
     [:div.Demo-box "Box"]
     [:div.Demo-box "Box"]
     [:div.Demo-box "Box"]]]])
