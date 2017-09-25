(ns ui.docs.buttons
  (:require [ui.elements :as element]
            [ui.layout :as layout]
            [clojure.spec :as spec]))

(defn documentation []
  [element/article
   "### Button  
   Create Actionable Items of Different Flavours

   "
   [layout/horizontally
    [element/button {:flat? true
                     :fill? true} "Foo"]
    [element/button {:class "primary"} [:span "Bar"]]
    [element/button {:class "secondary" :rounded? true} "Baz"]
    [element/button {:rounded? true :flat? true} "Qux"]
    [element/button {:class "secondary"}
     [element/icon {:font "ion"} "ios-settings"]
     "Settings"]
    [element/button {:flat?     true
                     :circular? true
                     :title     "Un-lock"}
     [element/icon {:font "material-icons"} "fingerprint"]]]
   (let [specs (apply hash-map (rest (spec/form :ui.element.button/params)))]
     [:pre (pr-str (:req-un specs))]
     [:pre (pr-str (zipmap (map name (:opt-un specs))
                           (map (comp name spec/form)
                                (:opt-un specs))))])
   [:p
    [:em
     [:small
      "Note that ui does not come with an icon-library of it's own, so
    you'll need to include one yourself and register it with ui. Have
    a look at the icon-element for further explanation."]]]])


