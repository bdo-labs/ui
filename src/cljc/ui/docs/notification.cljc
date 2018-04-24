(ns ui.docs.notification
  (:require [ui.element.notification.spec :as spec]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.element.showcase.views :refer [showcase]]))


(defn documentation []
  [layout/vertically {:background  :white
                      :scrollable? true}
   (fn []
     [element/article
      "### Notification(s)

       Show notifications based on outside input
       "
      #_[layout/vertically
       [element/numberfield {:label "Max number"
                             :model -max}]
       [element/numberfield {:label "Min number"
                             :model -min}]
       [element/numberfield {:label "Step"
                             :model -step}]
       [element/numberfield {:placeholder "Type in your number here"
                             :max @-max
                             :min @-min
                             :step @-step
                             :model model}]]])])
