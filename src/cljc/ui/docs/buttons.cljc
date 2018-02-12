(ns ui.docs.buttons
  (:require [ui.elements :as element]
            [ui.layout :as layout]
            [clojure.spec.alpha :as spec]
            [clojure.pprint :as pprint]
            [ui.util :as util]))

(defn documentation []
  [element/article
   "### Button  
   Create Actionable Items of Different Flavours

   "
   [layout/horizontally
    [element/button {:flat true} "Foo"]
    [element/button {:class  "primary" :ripple true} [:span "Bar"]]
    [element/button {:class "secondary" :rounded true :disabled true} "Baz"]
    [element/button {:raised true :rounded true :flat true} "Qux"]
    [element/button {:class  "secondary"} [element/icon {:font "ion"} "ios-settings"] "Settings"]
    [element/button {:flat true :circular true :title "Un-lock"} [element/icon {:font "material-icons"} "fingerprint"]]] 
   "_Note that ui does not come with an icon-library of it's own, so you'll need to include one yourself and register it with ui. Have a look at the icon-element for further explanation._"])


