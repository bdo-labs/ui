(ns ui.docs.colors
  #?(:cljs (:require-macros [garden.def :refer [defcssfn defkeyframes]]))
  (:require #?(:clj [garden.def :refer [defcssfn defkeyframes]])
            [re-frame.core :as re-frame]
            [garden.color :as color]
            [garden.units :as unit]
            [garden.core :refer [css]]
            [ui.element.content :refer [article]]
            [ui.styles :as styles]
            [ui.layout :as layout]
            [ui.element.color-picker :refer [color-picker]]
            [ui.util :as util]))



(defcssfn linear-gradient)


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
       (str (color/hex (-> styles/theme :default :primary))))))


(re-frame/reg-sub
 ::secondary
 (fn [db [k]]
   (or (get db k)
       (str (color/hex (-> styles/theme :default :secondary))))))


(defn documentation []
  (let [primary       (re-frame/subscribe [::primary])
        secondary     (re-frame/subscribe [::secondary])
        set-primary   #(re-frame/dispatch [::set-primary %])
        set-secondary #(re-frame/dispatch [::set-secondary %])]
    (fn []
      (let [primary   (str @primary)
            secondary (str @secondary)]
        #?(:cljs
           (let [background (css [:body {:background                (linear-gradient (unit/deg 45) primary secondary)
                                         :background-size           [[(unit/percent 400) (unit/percent 400)]]
                                         :animation                 [[:move-background :30s :ease]]
                                         :animation-iteration-count :infinite}])]
             (styles/inject js/document "--colors" background)))
        [article
         "### Color-picker
      Pick and choose colors that can easily be persisted as a theme "
         [layout/horizontally
          [layout/vertically
           [:span "Primary"]
           [color-picker {:hex       primary
                          :class     "raised"
                          :on-change set-primary}]]
          [layout/vertically
           [:span "Secondary"]
           [color-picker {:hex       secondary
                          :class     "raised"
                          :on-change set-secondary}]]]
         [layout/centered
          [:small [:em (str "Note that clicking the color-value changes it's type, so you can copy the version you prefer to your clip-board")]]]]))))
