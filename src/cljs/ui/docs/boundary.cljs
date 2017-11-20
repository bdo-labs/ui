(ns ui.docs.boundary
  (:require [reagent.core :refer [atom]]
            [ui.elements :as element]
            [ui.virtuals :as virtual]
            [ui.layout :as layout]
            [re-frame.core :as re-frame]))


(defn documentation []
  (let [id            "button-boundary"
        click-outside @(re-frame/subscribe [:ui.virtual.boundary/click-outside id])]
    [element/article
     "### Boundary

   Boundary as implied by it's name will find the boundary of it's
   immidiate child. You can then use that boundary to apply interaction
   that are not natively available in the DOM. Such as knowing when the
   element is within the viewport or when your clicking on the outside
   of it.
   "
     [:pre {:style {:background-color (if click-outside "red" "")}}
      (pr-str click-outside)]
     [virtual/boundary {:id     id
                        :offset [10 -5]
                        :lift   true}
      [element/button {:class "primary"}
       "Move mouse-pointer here"]]]))

