(ns ui.docs.date-picker
  (:require [#?(:clj clj-time.core :cljs cljs-time.core) :as t]
            [#?(:clj clj-time.format :cljs cljs-time.format) :as fmt]
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.util :as u]
            [ui.util :as util]))


(re-frame/reg-event-db ::set-date (fn [db [_ date]] (assoc db ::date date)))
(re-frame/reg-event-db ::toggle-short-form (fn [db [_]] (update-in db [::short-form?] not)))
(re-frame/reg-event-db ::set-jump (fn [db [_ jump]] (assoc db ::jump (int jump))))
(re-frame/reg-event-db ::set-period (fn [db [_ period]] (assoc db ::period period)))


(re-frame/reg-sub ::date (fn [db] (or (::date db) (t/now))))
(re-frame/reg-sub ::jump (fn [db] (or (::jump db) 1)))
(re-frame/reg-sub ::short-form? (fn [db] ^boolean (::short-form? db)))
(re-frame/reg-sub ::period (fn [db] (::period db)))


(defn datepicker []
  (let [date        @(re-frame/subscribe [::date])
        on-click    #(re-frame/dispatch [::set-date %])]
    [element/date-picker
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
