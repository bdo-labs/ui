(ns ui.docs.icons
  (:require [ui.elements :as element]
            [ui.layout :as layout]
            [garden.color :as color]))

(defn documentation []
  [element/article
   "# Icons
   Add some Flare with a Decent set of Symbols
   "
   [layout/centered {:raised? true
                         :rounded? true
                         :inline? true}
    [element/icon {:font "ion"
                   :size 7} "happy-outline"]
    [element/icon {:font "ion"
                   :size 5} "coffee"]
    [element/icon {:font "material-icons"
                   :size 5} "cake"]]])
