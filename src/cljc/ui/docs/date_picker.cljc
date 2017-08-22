(ns ui.docs.date-picker
  (:require [#?(:clj clj-time.core :cljs cljs-time.core) :as t]
            [#?(:clj clj-time.format :cljs cljs-time.format) :as fmt]
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as util]))


(re-frame/reg-event-db ::set-date (fn [db [_ date]] (assoc db ::date date)))
(re-frame/reg-event-db ::toggle-short-form (fn [db [_]] (update-in db [::short-form?] not)))
(re-frame/reg-event-db ::set-jump (fn [db [_ jump]] (assoc db ::jump (int jump))))
(re-frame/reg-event-db ::set-period (fn [db [_ period]] (assoc db ::period period)))


(re-frame/reg-sub ::date (fn [db] (or (::date db) (t/now))))
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
        picker         @(re-frame/subscribe [::picker])
        date           @(re-frame/subscribe [::date])
        formatted-date (fmt/unparse (fmt/formatter "dd. MMM. yyyy") date)
        set-date       #(do (re-frame/dispatch [::set-date %])
                            (close-dialog))]
    [layout/vertically
     [layout/horizontally
      [element/textfield {:read-only? true
                          :value      formatted-date
                          :style      {:flex 1}
                          :on-click   toggle-dialog}]
      [element/icon {:font "ion"} "ios-calendar-outline"]]
     [element/dialog {:show?  show-dialog
                      :cancel close-dialog}
      [layout/vertically  {:fill? true}
       [layout/horizontally
        [element/button {:on-click #(re-frame/dispatch [::set-picker :months])} "months"]
        [element/button {:on-click #(re-frame/dispatch [::set-picker :two-months])} "2 months"]
        ]
       (case picker
         (:months :two-months) [element/months {:every    (case picker
                                                            :two-months 2
                                                            :quarter    3
                                                            :tertial    4
                                                            1)
                                                :on-click #(set-date (t/first-day-of-the-month %))}]
         [element/years {:on-click set-date}])]]]

    #_[element/date-picker
       {:selected      date
        :nav?          true
        :on-click      on-click
        :on-navigation #(re-frame/dispatch [::set-date (t/first-day-of-the-month %)])}]))


(defn documentation []
  (let [period @(re-frame/subscribe [::period])
        date   @(re-frame/subscribe [::date])]
    [element/article
     "## Date-picker
     Our date-picker expands out of a regular text-input upon focus.
     Within the calendar you can click on a certain date or you can
     enable ranges within the month you display.
     "
     [:span
      [:input#jump {:type         :number
                    :min          1
                    :max          12
                    :defaultValue 1
                    :on-change    #(re-frame/dispatch [::set-jump (.-value (.-target %))])}]
      [:label {:for :jump} "Number of months to jump at a time"]]
     [:span
      [:input#short-form {:type     :checkbox
                          :on-click #(re-frame/dispatch [::toggle-short-form])}]
      [:label {:for :short-form} "Display short-form of weekdays"]]
     [datepicker]
     (when (not-empty period)
       (let [from (:from period)
             to   (:to period)]
         [:small (str (fmt/unparse (fmt/formatter "dd. MMM. yyyy") from) " - "
                      (fmt/unparse (fmt/formatter "dd. MMM. yyyy") to))]))]))
