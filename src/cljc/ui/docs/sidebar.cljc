(ns ui.docs.sidebar
  (:require [clojure.string :as str]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as util]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-db ::toggle-backdrop util/toggle)
(re-frame/reg-event-db ::toggle-ontop util/toggle)
(re-frame/reg-event-db ::toggle-open util/toggle)

(re-frame/reg-event-db
 ::set-to-the
 (fn [db [_ alignment]]
   (assoc db ::to-the alignment)))

(re-frame/reg-sub ::backdrop util/extract-or-false)
(re-frame/reg-sub ::open util/extract-or-false)
(re-frame/reg-sub ::ontop util/extract-or-false)
(re-frame/reg-sub ::to-the util/extract)

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
     [layout/vertically {:fill? true}
      [element/checkbox {:checked  open?
                         :on-click toggle-open} "Open?"]
      [element/checkbox {:checked  backdrop?
                         :on-click toggle-backdrop} "Backdrop?"]
      [element/checkbox {:checked  ontop?
                         :on-click toggle-ontop} "Ontop?"]
      [:span
       [:label {:for :to-the} "To the "]
       [:select#to-the {:defaultValue "left" :on-change set-to-the}
        [:option {:value "left"} "Left"]
        [:option {:value "right"} "Right"]]]
      [layout/vertically {:class "demo" :fill? true}
       [element/sidebar {:open open? :backdrop backdrop? :ontop ontop? :to-the (or to-the :left) :fill true}
        [layout/vertically {:fill? true}
         [:h3 "Sidebar content"]]
        [layout/vertically {:fill? true}
         [:h3 "Main content"]]]]]]))
