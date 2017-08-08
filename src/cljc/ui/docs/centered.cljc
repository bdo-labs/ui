(ns ui.docs.centered
  (:require [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as u]))


(re-frame/reg-event-db ::toggle-fill? u/toggle)


(re-frame/reg-sub ::fill? u/extract-or-false)


(defn documentation
  []
  [element/article
   "### Centered

     As the name suggests, this layout will center it's children  
     vertically and horizontally dead-center.

     "
   [layout/vertically {:fill? true :gap? false}
    [layout/centered {:class "demo"}
     [:div.Demo-box "Box"]
     [:div.Demo-box "Box"]
     [:div.Demo-box "Box"]]]])
