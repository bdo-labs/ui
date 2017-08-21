(ns ui.docs.horizontally
  (:require [ui.elements :as element]
            [ui.layout :as layout]))


(defn documentation
  []
  [element/article
   "### Horizontally
   
   By default in a horizontal layout, the children are layed out left 
   to right, top to bottom.

   "
   [layout/vertically {:fill? true :gap? false}
    [layout/horizontally {:class "demo" :fill? true}
     [:div.Demo-box "Box"]
     [:div.Demo-box "Box"]
     [:div.Demo-box "Box"]]]])
