(ns ui.docs.sidebar
  (:require [clojure.string :as str]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as u]
            [re-frame.core :as re-frame]))


(re-frame/reg-event-db ::toggle-backdrop u/toggle)
(re-frame/reg-event-db ::toggle-ontop u/toggle)
(re-frame/reg-event-db ::toggle-open u/toggle)


(re-frame/reg-event-db
 ::set-to-the
 (fn [db [_ alignment]]
   (assoc db ::to-the alignment)))


(re-frame/reg-sub ::backdrop u/extract-or-false)
(re-frame/reg-sub ::open u/extract-or-false)
(re-frame/reg-sub ::ontop u/extract-or-false)
(re-frame/reg-sub ::to-the u/extract)


(defn documentation
  []
  (let [open?           ^boolean @(re-frame/subscribe [::open])
        backdrop?       ^boolean @(re-frame/subscribe [::backdrop])
        ontop?          ^boolean @(re-frame/subscribe [::ontop])
        to-the          @(re-frame/subscribe [::to-the])
        toggle-open     #(re-frame/dispatch [::toggle-open])
        toggle-backdrop #(re-frame/dispatch [::toggle-backdrop])
        toggle-ontop    #(re-frame/dispatch [::toggle-ontop])
        set-to-the      #(re-frame/dispatch [::set-to-the (.-value (.-target %))])]
    [element/article
     "# Sidebar
     "
     [layout/vertically {:fill true}
      [element/checkbox {:checked? open?
                         :on-click toggle-open} "Open?"]
      [element/checkbox {:checked? backdrop?
                         :on-click toggle-backdrop} "Backdrop?"]
      [element/checkbox {:checked? ontop?
                         :on-click toggle-ontop} "Ontop?"]
      [:span
       [:label {:for :to-the} "To the "]
       [:select#to-the {:defaultValue "left" :on-change set-to-the}
        [:option {:value "left"} "Left"]
        [:option {:value "right"} "Right"]]]
      [layout/vertically {:class [:demo]}
       [element/sidebar {:open open? :backdrop backdrop? :ontop ontop? :to-the to-the}
        [layout/vertically
         [:h3 "Sidebar content"]]
        [layout/vertically
         [:h3 "Main content"]]]]]]))
