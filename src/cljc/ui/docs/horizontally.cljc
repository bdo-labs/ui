(ns ui.docs.horizontally
  (:require [ui.elements :as element]
            [ui.layout :as layout]))

(defn documentation
  []
  [layout/vertically {:background :white :fill? true :gap? false}
   [layout/horizontally {:class "demo" :style {:height "10rem"}}
    [:div.Demo-box "Box"]
    [:div.Demo-box "Box"]
    [:div.Demo-box "Box"]]
   [element/article
    "
## Horizontally

By default in a horizontal layout, the children are layed out left 
to right, top to bottom.

"]])
