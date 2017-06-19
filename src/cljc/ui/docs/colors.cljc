(ns ui.docs.colors
  (:require [re-frame.core :as re-frame]
            [garden.color :as color]
            [ui.element.content :refer [article]]
            [ui.styles :as styles]
            [ui.layout :as layout]
            [ui.element.color-picker :refer [color-picker]]
            [ui.util :as u]))


(re-frame/reg-event-db
 ::set-primary
 (fn [db [_ hex]]
   (assoc db ::primary hex)))


(re-frame/reg-event-db
 ::set-secondary
 (fn [db [_ hex]]
   (assoc db ::secondary hex)))


(re-frame/reg-sub
 ::primary
 (fn [db [k]]
   (or (get db k)
       (color/hex (-> styles/theme :default :primary)))))


(re-frame/reg-sub
 ::secondary
 (fn [db [k]]
   (or (get db k)
       (color/hex (-> styles/theme :default :secondary)))))


(defn documentation []
  (let [primary       @(re-frame/subscribe [::primary])
        secondary     @(re-frame/subscribe [::secondary])
        set-primary   #(re-frame/dispatch [::set-primary %])
        set-secondary #(re-frame/dispatch [::set-secondary %])]
    [article
     "# Color-picker
      Pick and choose colors that can easily be persisted as a theme"
     [layout/horizontally {:rounded? true
                           :raised?  true
                           :style    {:background "rgb(250,250,250)"}}
      [layout/vertically
       [:span "Primary"]
       [color-picker {:hex       primary
                      :on-change set-primary}]]
      [layout/vertically
       [:span "Secondary"]
       [color-picker {:hex       secondary
                      :on-change set-secondary}]]]]))
