(ns ui.element.calendar
  (:require [#?(:clj clj-time.coerce :cljs cljs-time.coerce) :as coerce]
            [#?(:clj clj-time.core :cljs cljs-time.core) :as t]
            [#?(:clj clj-time.format :cljs cljs-time.format) :as fmt]
            [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.string :as str]
            [re-frame.core :as re-frame]
            [ui.element.button :refer [button]]
            [ui.element.icon :refer [icon]]
            [ui.element.containers :refer [container]]
            [ui.util :as u]
            [ui.util :as util]
            [clojure.test.check.generators :as gen]
            [clojure.spec :as spec]))


(spec/def ::stub
  (spec/with-gen fn?
    (gen/return (constantly nil))))


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


(def weekdays (cycle ["Sunday" "Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday"]))


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
 util/extract)


(defn- before? [a b]
  (if (or (nil? a) (nil? b))
    false
    (> (int (coerce/to-epoch a))
       (int (coerce/to-epoch b)))))


(defn- after? [a b]
  (if (or (nil? a) (nil? b))
    false
    (not (before? a b))))


(defn- calendar-nav
  [{:keys [id jump on-click format !model]
    :or   {jump 1 format "MMMM yyyy"}
    :as   params}]
  (fn []
    (let [minimum?    #(after? % (:min params))
          maximum?    #(before? % (:max params))
          month       (t/months jump)
          less        (t/minus @!model month)
          more        (t/plus @!model month)
          on-previous #(do (reset! !model less)
                           (when (fn? on-click) (on-click less)))
          on-next     #(do (reset! !model more)
                           (when (fn? on-click) (on-click more)))]
      [container {:layout :horizontally
                  :gap?   false
                  :fill?  true
                  :align  [:center :center]
                  :space  :between
                  :class  "calendar-nav"}
       ^{:key "previous-month"} [icon {:font "ion" :on-click on-previous} "chevron-left"]
       ^{:key "current-month"} [:h3 (fmt/unparse (fmt/formatter format) @!model)]
       ^{:key "next-month"} [icon {:font "ion" :on-click on-next} "chevron-right"]])))


(defn years []
  (let [current-year (inc (util/parse-int (fmt/unparse (fmt/formatter "yyyy") (t/now))))
        rows         (->> (range (- current-year 4) current-year)
                          (partition 2))]
    [container {:layout :vertically}
     (for [years rows]
       [container {:layout :horizontally :fill? true :gap? false}
        (for [year years] [button {:fill? true} (str year)])])]))


(spec/def ::every (spec/and pos-int? #(int? (/ 12 %)) #(<= % 6)))
(spec/def ::selected inst?)
(spec/def ::on-click ::stub)
(spec/def ::months-params
  (spec/keys :req-un [::on-click]
             :opt-un [::selected ::every]))
(spec/def ::months-args
  (spec/cat :params ::months-params))


(defn months
  "Display the calendar-months of a [selected] year"
  [& args]
  (let [{:keys [params]}           (util/conform-or-fail ::months-args args)
        {:keys [every
                on-click
                selected]
         :or   {selected (t/now)}} params
        !model                     (atom selected)
        group-every                (if (= every 1) 3 every)]
    (fn [{:keys [params]}]
      (let [{:keys [every]
             :or   {every 1}} params
            year (int (fmt/unparse (fmt/formatter "yyyy") @!model))
            rows (->> (range 1 13)
                      (map #(t/date-time year % 1))
                      (partition group-every))]
        [container {:layout :vertically :align [:center :center]}
         [calendar-nav {:jump 12 :format "yyyy" :!model !model}]
         (for [months rows]
           [container {:key (str/join months) :layout :horizontally :fill? true :gap? false}
            (if (= every 1)
              (for [month months]
                (let [val (fmt/unparse (fmt/formatter "MMMM") month)]
                  [button {:key (str "btn-" val) :fill? true :on-click #(on-click month)} val]))
              (let [val (str (fmt/unparse (fmt/formatter "MMMM") (first months)) " - " (fmt/unparse (fmt/formatter "MMMM") (last months)))]
                [button {:key (str "btn-" val) :fill? true :on-click #(on-click months)} val]))])]))))


(spec/def ::start-of-week (spec/and pos-int? #(>= 0 %) #(<= 6)))
(spec/def ::show-weekend? boolean?)
(spec/def ::nav? boolean?)
(spec/def ::selectable? boolean?)
(spec/def ::short-form? boolean?)
(spec/def ::jump pos-int?)
(spec/def ::on-navigation ::stub)
(spec/def ::days-params
  (spec/keys :req-un [::on-click]
             :opt-un [::on-navigation
                      ::jump
                      ::short-form?
                      ::selectable?
                      ::nav?
                      ::show-weekend?
                      ::start-of-week]))
(spec/def ::days-args
  (spec/cat :params ::days-params))


(defn days
  [& args]
  (let [{:keys [params]} (util/conform-or-fail ::days-args args)
        {:keys [start-of-week selected show-weekend?
                jump on-click nav? selectable?
                on-navigation class short-form?]
         :or   {start-of-week 1
                selected      (t/now)
                jump          1
                short-form?   false
                nav?          true
                show-weekend? true
                selectable?   true}
         }               params
        num-days         (if (true? show-weekend?) 7 5)
        weekdays-num     (range start-of-week (+ start-of-week num-days))
        caret            (atom selected)]
    (fn []
      [:div.Calendar
       (when nav?
         [calendar-nav {:jump     jump
                        :min      (:min params)
                        :max      (:min params)
                        :!model   caret
                        :on-click on-navigation}])
       [:table {:class class}
        [:thead.Weekdays
         [:tr
          (for [weekday weekdays-num]
            (let [weekday-name (nth weekdays weekday)]
              ^{:key (str "weekday-" weekday)} [:th (if short-form? (subs weekday-name 0 3) weekday-name)]))]]
        [:tbody
         (for [week (weeks @caret)]
           ^{:key (str "week-" (:month (first week)) "-" (:day (first week)))}
           [:tr.Week
            (for [weekday weekdays-num]
              (let [day     (nth week (dec weekday))
                    dt      (day->date day)
                    classes [(case (:belongs-to-month day) :previous "Previous" :next "Next" "")
                             (when selectable? "Selectable")
                             (when (same-day? (date->day selected) day) "Selected")
                             (when (same-day? (date->day (t/now)) day) "Today")]]
                ^{:key (str "day-" (:day day))}
                [:td.Day {:class    (str/join " " classes)
                          :on-click #(when selectable? (on-click dt))}
                 [:span (:day day)]]))])]]])))
