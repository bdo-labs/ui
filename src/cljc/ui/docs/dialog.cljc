(ns ui.docs.dialog
  (:require [ui.layout :as layout]
            [ui.elements :as element]
            [ui.util :as u]
            [re-frame.core :refer [subscribe dispatch reg-sub reg-event-db]]))


(reg-event-db ::show? u/toggle)


(reg-sub ::show? u/extract-or-false)


(defn documentation []
  (let [show?      @(subscribe [::show?])
        on-click   #(dispatch [::show?])
        on-confirm #(dispatch [::show? false])]
    [element/article
     "# Dialog
      Think, quick feedback!
     "
     [element/button {:on-click on-click} "Show dialog"]
     [element/confirm-dialog {:show?      show?
                              :on-confirm on-confirm}
      "Are you sure you would like to proceed?"]]))
