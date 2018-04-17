(ns ui.docs.centered
  (:require [ui.elements :as element]
            [ui.layout :as layout]))

(defn documentation
  []
  [layout/vertically {:background :white
                      :fill? true
                      :gap? false}
   [layout/centered {:class "demo"
                     :style {:position :relative
                             :height "10rem"}}
    [:div.Demo-box "Box"]
    [:div.Demo-box "Box"]
    [:div.Demo-box "Box"]]
   [element/article
    "
## Centered

As the name suggests, this layout will center it's children  
vertically and horizontally dead-center.

"]])
