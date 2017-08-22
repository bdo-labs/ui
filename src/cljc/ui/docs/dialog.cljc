(ns ui.docs.dialog
  (:require [ui.layout :as layout]
            [ui.elements :as element]
            [ui.util :as u]
            [re-frame.core :as re-frame]))


(re-frame/reg-event-db
 ::set-show
 (fn [db [_ dialog]]
   (assoc db ::show dialog)))


(re-frame/reg-sub ::show u/extract)


(defn documentation []
  (let [show         @(re-frame/subscribe [::show])
        dialog-props {:backdrop?          true
                      :cancel-on-backdrop true}]
    [element/article
     "### Dialog
      Think quick feedback!
     "
     [element/button {:class    "secondary"
                      :on-click #(re-frame/dispatch [::set-show 1])} "Show dialog"]
     [element/dialog (-> {:show?  (= show 1)
                          :cancel #(re-frame/dispatch [::set-show false])}
                         (merge  dialog-props))
      [layout/vertically
       [:h2 "Are you sure you would like to proceed?"]
       [element/button {:on-click #(re-frame/dispatch [::set-show 2])} "Show dialog 2"]]]
     [element/dialog (-> {:show?  (= show 2)
                          :cancel #(re-frame/dispatch [::set-show 1])}
                         (merge dialog-props))
      [layout/vertically
       [:h2 "You did in-fact proceed :)"]
       [element/button {:on-click #(re-frame/dispatch [::set-show 1])} "Show dialog 1"]]]]))
