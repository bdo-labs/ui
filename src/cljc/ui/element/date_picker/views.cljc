(ns ui.element.date-picker.views
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [#?(:clj clj-time.coerce :cljs cljs-time.coerce) :as coerce]
            [#?(:clj clj-time.core :cljs cljs-time.core) :as t]
            [#?(:clj clj-time.format :cljs cljs-time.format) :as fmt]
            [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [re-frame.core :as re-frame]
            [clojure.core.async :as async :refer [<! #?(:clj go) timeout]]
            [ui.element.containers.views :refer [container]]
            [ui.element.icon.views :refer [icon]]
            [ui.element.calendar.views :as calendar]
            [ui.element.textfield.views :refer [textfield]]
            [ui.util :as util]))

;; TODO Enable free-editing of text-field
;; TODO Make handlers and subscriptions unique to each picker
(defn date-picker
  [{:keys [on-click on-focus on-navigation
           selected nav? selectable?
           show-weekend? short-form? jump]
    :or   {nav?          true
           jump          1
           selected      (t/now)
           short-form?   false
           selectable?   true
           show-weekend? true}
    :as   params}]
  (let [!open?            (atom false)
        on-internal-click #(when (fn? on-click) (on-click %))]
    (fn []
      [:div.Date-picker
       [container {:layout :vertically}
        [container {:layout :horizontally
                    :fill?  true
                    :align  [:center :center]}
         [textfield {:value    (fmt/unparse (fmt/formatter "dd. MMM. yyyy") selected)
                     :style    {:flex 1}
                     :on-focus #(do (reset! !open? true)
                                    (when (fn? on-focus) (on-focus %)))}]
         [icon {:font "ion"} "ios-calendar-outline"]]
        (when @!open?
          [calendar/days {:selected      selected
                          :on-click      on-internal-click
                          :selectable?   selectable?
                          :short-form?   short-form?
                          :show-weekend? show-weekend?
                          :on-navigation on-navigation
                          :jump          jump
                          :nav?          nav?
                          :min           (:min params)
                          :max           (:min params)}])]])))
