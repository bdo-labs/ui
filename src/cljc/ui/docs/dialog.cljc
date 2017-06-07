(ns ui.docs.dialog
  (:require [ui.layout :as layout]
            [ui.elements :as element]
            [ui.util :as u]
            [re-frame.core :refer [subscribe dispatch reg-sub reg-event-db]]))


(reg-event-db ::open? u/toggle)


(reg-sub ::open? u/extract)


(defn documentation []
  (let [open?      @(subscribe [::open?])
        on-click   #(dispatch [::open?])
        on-confirm #(dispatch [::open? false])]
    [element/article
     "# Dialog
      Think, quick feedback!
     "
     [element/button {:on-click on-click} "Open dialog"]
     [element/dialog {:open    open?
                      :confirm on-confirm}
      "Are you sure you would like to proceed?"]]))
