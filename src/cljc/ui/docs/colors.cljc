(ns ui.docs.colors
  #?(:cljs (:require-macros [garden.def :refer [defcssfn defkeyframes]]))
  (:require #?(:clj [garden.def :refer [defcssfn defkeyframes]])
            [re-frame.core :as re-frame]
            [garden.color :as color]
            [garden.units :as unit]
            [garden.core :refer [css]]
            [ui.styles :as styles]
            [ui.layout :as layout]
            [ui.element.content.views :refer [article]]
            [ui.element.color-picker.views :refer [color-picker]]
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
           (let [background (css [:.Container.Header {:background [[primary :!important]]}])]
             (styles/inject js/document "--colors" background)))
        [layout/vertically {:background :white
                            :fill?      true
                            :gap?       false}
         [layout/centered {:class "demo"
                           :style {:position :relative
                                   :height   "6rem"}}
          [color-picker {:hex       primary
                         :class     "raised"
                         :on-change set-primary}]]
         [article
          "### Color-picker

Pick and choose colors that can easily be persisted as a theme"]]))))
