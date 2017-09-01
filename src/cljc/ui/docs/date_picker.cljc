(ns ui.docs.date-picker
  (:require [#?(:clj clj-time.core :cljs cljs-time.core) :as t]
            [#?(:clj clj-time.format :cljs cljs-time.format) :as fmt]
            [#?(:clj clj-time.coerce :cljs cljs-time.coerce) :as coerce]
            [#?(:clj clj-time.predicates :cljs cljs-time.predicates) :refer [same-date?]]
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as util]))


(re-frame/reg-event-db ::set-date (fn [db [_ date]] (assoc db ::date date)))
(re-frame/reg-event-db ::toggle-short-form (fn [db [_]] (update-in db [::short-form?] not)))
(re-frame/reg-event-db ::set-jump (fn [db [_ jump]] (assoc db ::jump (int jump))))
(re-frame/reg-event-db ::set-period (fn [db [_ period]] (assoc db ::period period)))


(re-frame/reg-sub ::date (fn [db] (or (::date db) [(t/now) (t/now)])))
(re-frame/reg-sub ::jump (fn [db] (or (::jump db) 1)))
(re-frame/reg-sub ::short-form? (fn [db] ^boolean (::short-form? db)))
(re-frame/reg-sub ::period (fn [db] (::period db)))




(re-frame/reg-sub
 ::show-dialog
 (fn [db _] (or (::show-dialog db) false)))


(re-frame/reg-sub
 ::picker
 util/extract)


(re-frame/reg-event-db
 ::toggle-dialog
 (fn [db _] (update db ::show-dialog not)))


(re-frame/reg-event-db
 ::close-dialog
 (fn [db _] (assoc db ::show-dialog false)))


(re-frame/reg-event-db
 ::set-picker
 (fn [db [_ picker]] (assoc db ::picker picker)))


(defn datepicker []
  (let [show-dialog    @(re-frame/subscribe [::show-dialog])
        toggle-dialog  #(re-frame/dispatch [::toggle-dialog])
        close-dialog   #(re-frame/dispatch [::close-dialog])
        picker         (or @(re-frame/subscribe [::picker]) :years)
        date           @(re-frame/subscribe [::date])
        formatted-date (if (= (fmt/unparse (fmt/formatter "dd. MMM. yyyy") (first date)) (fmt/unparse (fmt/formatter "dd. MMM. yyyy") (last date)))
                         (fmt/unparse (fmt/formatter "dd. MMM. yyyy") (first date))
                         (str (fmt/unparse (fmt/formatter "dd. MMM. yyyy") (first date)) " - " (fmt/unparse (fmt/formatter "dd. MMM. yyyy") (last date))))
        set-date       #(do (re-frame/dispatch [::set-date %])
                            (close-dialog))]
    [layout/vertically
     [layout/horizontally {:fill? true}
      [element/textfield {:read-only? true
                          :value      formatted-date
                          :style      {:flex 1}
                          :on-click   toggle-dialog}]
      [element/icon {:font "ion"} "ios-calendar-outline"]]
     [element/dialog {:show?  show-dialog
                      :cancel close-dialog}
      [layout/vertically  {:fill? true
                           :style {:min-width "390px"}}
       [layout/horizontally {:gap?     false
                             :align    [:center :center]
                             :fill?    true
                             :compact? true}
        [element/button {:class (str "Tab" (when (= picker :day) " active")) :on-click #(re-frame/dispatch [::set-picker :day])} "day"]
        [element/button {:class (str "Tab" (when (= picker :months) " active")) :on-click #(re-frame/dispatch [::set-picker :months])} "months"]
        [element/button {:class (str "Tab" (when (= picker :twomonths) " active")) :on-click #(re-frame/dispatch [::set-picker :twomonths])} "2 months"]
        [element/button {:class (str "Tab" (when (= picker :quarter) " active")) :on-click #(re-frame/dispatch [::set-picker :quarter])} "quarter"]
        [element/button {:class (str "Tab" (when (= picker :tertial) " active")) :on-click #(re-frame/dispatch [::set-picker :tertial])} "tertial"]
        [element/button {:class (str "Tab" (when (= picker :years) " active")) :on-click #(re-frame/dispatch [::set-picker :years])} "years"]]
       (case picker
         :day
         [element/days {:on-click    set-date
                        :short-form? true}]
         (:months :twomonths :quarter :tertial)
         [element/months {:every    (case picker
                                      :twomonths 2
                                      :quarter   3
                                      :tertial   4
                                      1)
                          :selected (first date)
                          :on-click set-date}]
         [element/years {:on-click set-date}])]]]))


(defn documentation []
  (let [date @(re-frame/subscribe [::date])]
    [element/article
     "## Date-picker
     Our date-picker expands out of a regular text-input upon focus.
     Within the calendar you can click on a certain date or you can
     enable ranges within the month you display.
     "
     [datepicker]]))
