(ns ui.docs.dialog
  (:require [ui.layout :as layout]
            [ui.elements :as element]
            [ui.util :as u]
            [re-frame.core :refer [subscribe dispatch reg-sub reg-event-db]]))


(reg-event-db ::show? u/toggle)


(reg-sub ::show? u/extract-or-false)


(defn documentation []
  (let [show?    @(subscribe [::show?])
        on-click #(dispatch [::show?])
        cancel   #(dispatch [::show? false])]
    [element/article
     "### Dialog
      Think, quick feedback!
     "
     [element/button {:class "secondary"
                      :on-click on-click} "Show dialog"]
     [element/dialog {:show?               show?
                      :backdrop?           true
                      :cancel              cancel
                      :cancel-on-backdrop? false}
      [:h4 "Are you sure you would like to proceed?"]]]))
