(ns ui.element.calendar
  (:require [#?(:clj clj-time.coerce :cljs cljs-time.coerce) :as coerce]
            [#?(:clj clj-time.core :cljs cljs-time.core) :as t]
            [#?(:clj clj-time.format :cljs cljs-time.format) :as fmt]
            [clojure.string :as str]
            [re-frame.core :as re-frame]
            [ui.element.button :refer [button]]
            [ui.element.containers :refer [container]]
            [ui.util :as u]))


(defn- previous-month-days
  [date]
  (let [current-month (t/date-time (t/year date) (t/month date))
        weekday-current-month (t/day-of-week current-month)
        previous-month (t/minus current-month (t/months 1))
        last-day (t/number-of-days-in-the-month previous-month)
        days-to-fill (range (inc (- last-day (dec weekday-current-month))) (inc last-day))]
    (mapv (fn [d] {:day d
                   :month (- 1 (t/month date))
                   :year (t/year date)
                   :belongs-to-month :previous}) days-to-fill)))


(defn- current-month-days
  [date]
  (let [last-day (t/number-of-days-in-the-month date)]
    (mapv (fn [d]
            {:day d
             :month (t/month date)
             :year (t/year date)
             :belongs-to-month :current})
          (range 1 (inc last-day)))))


(defn- next-month-days
  [date]
  (let [current-month (t/date-time (t/year date) (t/month date))
        last-day-number (t/number-of-days-in-the-month current-month)
        last-day (t/date-time (t/year current-month) (t/month current-month) last-day-number)
        weekday-last-day (t/day-of-week last-day)
        days-to-fill (range 1 (inc (- 14 weekday-last-day)))]
    (mapv (fn [d] {:day d
                   :month (+ 1 (t/month date))
                   :year (t/year date)
                   :belongs-to-month :next}) days-to-fill)))


(defn- weeks
  [date]
  (let [previous-days (previous-month-days date)
        currrent-days (current-month-days date)
        next-days (next-month-days date)
        days (vec (concat previous-days currrent-days next-days))]
    (subvec (mapv vec (partition 7 days)) 0 6)))


(def days (cycle ["Sunday" "Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday"]))


(defn- date->day [date]
  (->> (str/split (fmt/unparse (fmt/formatter "d M yyyy") date) #" ")
       (map int)
       (zipmap [:day :month :year])))


(defn- day->date
  [day]
  (t/date-time (:year day) (:month day) (:day day)))


(defn- same-day? [needle haystack]
  (and (= (:day needle) (:day haystack))
       (= (:month needle) (:month haystack))
       (= (:year needle) (:year haystack))))


(re-frame/reg-event-db
 ::set-position
 (fn [db [_ position]]
   (assoc db ::position position)))


(re-frame/reg-sub
 ::position
 (fn [db [_ position]]
   (or (::position db)
       position)))


(defn before? [a b]
  (> (int (coerce/to-epoch a))
     (int (coerce/to-epoch b))))


(defn after? [a b]
  (not (before? a b)))


(defn- calendar-nav
  [{:keys [id jump on-click] :or {jump 1} :as params}]
  (let [position    @(re-frame/subscribe [::position (t/now)])
        minimum?    #(after? % (:min params))
        maximum?    #(before? % (:max params))
        month       (t/months jump)
        less        (t/minus position month)
        more        (t/plus position month)
        on-previous #(do (re-frame/dispatch [::set-position less])
                         (when (fn? on-click) (on-click less))
                         #?(:cljs (.focus (.getElementById js/document id))))
        on-next     #(do (re-frame/dispatch [::set-position more])
                         (when (fn? on-click) (on-click more))
                         #?(:cljs (.focus (.getElementById js/document id))))]
    [container {:direction "row" :align "center" :justify "space-between" :class [:calendar-nav]}
     ^{:key "previous-month"} [button {:disabled (minimum? less) :on-click on-previous :flat true} "<"]
     ^{:key "current-month"} [:h3 (fmt/unparse (fmt/formatter "MMMM yyyy") position)]
     ^{:key "next-month"} [button {:disabled (maximum? more) :on-click on-next :flat true} ">"]]))


(defn calendar
  [{:keys [start-of-week
           weekend
           placeholder
           jump
           on-click
           value
           nav?
           selectable?
           on-navigation
           class
           short-form?]
    :or   {start-of-week 1
           nav?           true
           weekend       true
           selectable?   #(true? true)}
    :as   params}]
  (let [position (re-frame/subscribe [::position (t/now)])
        minimum  (or (:min params) (t/minus (t/now) (t/years 100)))
        maximum  (or (:max params) (t/plus (t/now) (t/years 100)))]
    (fn [{:keys [value selectable?]}]
      (let [num-days (if (true? weekend) 7 5)
            weekdays (range start-of-week (+ start-of-week num-days))
            caret    (atom (or @position value))]
        [:div.Calendar
         (when nav?
           [calendar-nav {:id       (u/slug placeholder)
                          :jump     jump
                          :min      minimum
                          :max      maximum
                          :on-click on-navigation}])
         [:table {:class (u/names->str class)}
          [:thead.Weekdays
           [:tr
            (for [weekday weekdays]
              (let [weekday-name (nth days weekday)]
                ^{:key (str "weekday-" weekday)} [:th (if short-form? (subs weekday-name 0 3) weekday-name)]))]]
          [:tbody
           (for [week (weeks @caret)]
             ^{:key (str "week-" (:month (first week)) "-" (:day (first week)))}
             [:tr.Week
              (for [weekday weekdays]
                (let [day     (nth week (dec weekday))
                      dt      (day->date day)
                      classes [(case (:belongs-to-month day) :previous "Previous" :next "Next" "")
                               (when (selectable? (day->date day)) "Selectable")
                               (when (same-day? (date->day value) day) "Selected")
                               (when (same-day? (date->day (t/now)) day) "Today")]]
                  ^{:key (str "day-" (:day day))}
                  [:td.Day {:class    (str/join " " classes)
                            :on-click #(when (selectable? dt) (on-click dt))}
                   [:span (:day day)]]))])]]]))))
