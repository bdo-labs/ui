(ns ui.docs.vertically
  (:require [ui.elements :as element]
            [ui.layout :as layout]))


(defn documentation
  []
  [element/article
   "### Vertically"
   [layout/vertically {:fill? true :gap? false}
    [layout/vertically {:class "demo" :fill? true}
     [:div.Demo-box "Box"]
     [:div.Demo-box "Box"]
     [:div.Demo-box "Box"]]]])
