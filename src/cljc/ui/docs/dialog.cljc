(ns ui.docs.dialog
  (:require [ui.layout :as layout]
            [ui.elements :as element]
            [ui.util :as util]
            [re-frame.core :as re-frame]))


(re-frame/reg-event-db
 ::set-show
 (fn [db [_ dialog]]
   (assoc db ::show dialog)))


(re-frame/reg-sub ::show util/extract)


(defn documentation []
  (let [show         @(re-frame/subscribe [::show])
        dialog-props {:backdrop?          true
                      :cancel-on-backdrop true}
        show-dialog  (fn [n] #(re-frame/dispatch [::set-show n]))]
    [element/article
     "### Dialog
      Think quick feedback!
     "
     [layout/horizontally
      [element/button {:class    "secondary"
                       :on-click #(re-frame/dispatch [::set-show 1])}
       "Show dialog"]
      [element/button {:class    "secondary"
                       :on-click (show-dialog 3)}
       "Show confirmation"]]
     [element/dialog (-> {:show?  (= show 1)
                          :on-cancel #(re-frame/dispatch [::set-show false])}
                         (merge  dialog-props))
      [layout/vertically
       [:h2 "Are you sure you would like to proceed?"]
       [element/button {:on-click #(re-frame/dispatch [::set-show 2])} "Show dialog 2"]]]
     [element/dialog (-> {:show?  (= show 2)
                          :on-cancel #(re-frame/dispatch [::set-show 1])}
                         (merge dialog-props))
      [layout/vertically
       [:h2 "You did in-fact proceed :)"]
       [element/button {:on-click #(re-frame/dispatch [::set-show 1])} "Show dialog 1"]]]
     [element/confirm-dialog {:show? (= show 3)
                              :on-confirm #(util/log "Yay")
                              :on-cancel #(re-frame/dispatch [::set-show false])}
      "Are you sure?"]]))
