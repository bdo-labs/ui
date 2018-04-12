(ns ui.docs.vertically
  (:require [ui.elements :as element]
            [ui.layout :as layout]))

(defn documentation
  []
  [layout/vertically {:background :white :fill? true :gap? false}
   [layout/vertically {:class "demo" :style {:height "10rem"}}
    [:div.Demo-box "Box"]
    [:div.Demo-box "Box"]
    [:div.Demo-box "Box"]]
   [element/article
    "
## Vertically

By default in a vertical layout, the children are layed out top to
bottom, left to right.
"]])
