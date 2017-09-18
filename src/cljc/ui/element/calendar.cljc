(ns ui.element.calendar
  (:require [#?(:clj clj-time.coerce :cljs cljs-time.coerce) :as coerce]
            [#?(:clj clj-time.core :cljs cljs-time.core) :as time]
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


;; Specifications ---------------------------------------------------------


(spec/def ::stub
  (spec/with-gen fn?
    (gen/return (constantly nil))))

(spec/def ::start-of-week (spec/and pos-int? #(>= 0 %) #(<= 6)))
(spec/def ::show-weekend? boolean?)
(spec/def ::nav? boolean?)
(spec/def ::selectable? boolean?)
(spec/def ::multiple? boolean?)
(spec/def ::short-form? boolean?)
(spec/def ::jump pos-int?)
(spec/def ::on-navigation ::stub)
(spec/def ::every (spec/and pos-int? #(int? (/ 12 %)) #(<= % 6)))
(spec/def ::selected #?(:clj inst? :cljs time/date?))
(spec/def ::on-click ::stub)


(spec/def ::years-params
  (spec/keys :opt-un [::on-click]))
(spec/def ::years-args (spec/cat :params ::years-params))


(spec/def ::months-params
  (spec/keys :req-un [::on-click]
             :opt-un [::selected ::every]))
(spec/def ::months-args
  (spec/cat :params ::months-params))


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


;; Helper functions -------------------------------------------------------


(defn- previous-month-days
  [date]
  (let [current-month (time/date-time (time/year date) (time/month date))
        weekday-current-month (time/day-of-week current-month)
        previous-month (time/minus current-month (time/months 1))
        last-day (time/number-of-days-in-the-month previous-month)
        days-to-fill (range (inc (- last-day (dec weekday-current-month))) (inc last-day))]
    (mapv (fn [d] {:day d
                   :month (- 1 (time/month date))
                   :year (time/year date)
                   :belongs-to-month :previous}) days-to-fill)))


(defn- current-month-days
  [date]
  (let [last-day (time/number-of-days-in-the-month date)]
    (mapv (fn [d]
            {:day d
             :month (time/month date)
             :year (time/year date)
             :belongs-to-month :current})
          (range 1 (inc last-day)))))


(defn- next-month-days
  [date]
  (let [current-month (time/date-time (time/year date) (time/month date))
        last-day-number (time/number-of-days-in-the-month current-month)
        last-day (time/date-time (time/year current-month) (time/month current-month) last-day-number)
        weekday-last-day (time/day-of-week last-day)
        days-to-fill (range 1 (inc (- 14 weekday-last-day)))]
    (mapv (fn [d] {:day d
                   :month (+ 1 (time/month date))
                   :year (time/year date)
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
  (time/date-time (:year day) (:month day) (:day day)))


(defn- same-day? [needle haystack]
  (and (= (:day needle) (:day haystack))
       (= (:month needle) (:month haystack))
       (= (:year needle) (:year haystack))))


(defn- before? [a b]
  (if (or (nil? a) (nil? b))
    false
    (> (int (coerce/to-epoch a))
       (int (coerce/to-epoch b)))))


(defn- after? [a b]
  (if (or (nil? a) (nil? b))
    false
    (not (before? a b))))


;; Events -----------------------------------------------------------------


(re-frame/reg-event-db
 ::set-position
 (fn [db [_ position]]
   (assoc db ::position position)))


;; Subscriptions ----------------------------------------------------------


(re-frame/reg-sub
 ::position
 util/extract)



;; Views ------------------------------------------------------------------


(defn- calendar-nav
  [{:keys [id jump on-click format !model]
    :or   {jump 1 format "MMMM yyyy"}
    :as   params}]
  (fn []
    (let [minimum?    #(after? % (:min params))
          maximum?    #(before? % (:max params))
          month       (time/months jump)
          less        (time/minus @!model month)
          more        (time/plus @!model month)
          on-previous #(do (reset! !model less)
                           (when (fn? on-click) (on-click less)))
          on-next     #(do (reset! !model more)
                           (when (fn? on-click) (on-click more)))]
      [container {:layout :horizontally
                  :gap?   true
                  :fill?  true
                  :align  [:center :center]
                  :space  :between
                  :class  "calendar-nav"}
       ^{:key "previous-month"} [icon {:font "ion" :on-click on-previous} "chevron-left"]
       ^{:key "current-month"} [:h3 (fmt/unparse (fmt/formatter format) @!model)]
       ^{:key "next-month"} [icon {:font "ion" :on-click on-next} "chevron-right"]])))


(defn years [& args]
  (let [{:keys [params]}   (util/conform-or-fail ::years-args args)
        {:keys [on-click]} params
        current-year       (inc (util/parse-int (fmt/unparse (fmt/formatter "yyyy") (time/now))))
        rows               (->> (range (- current-year 4) current-year)
                                (partition 2))]
    [container {:layout :vertically
                :fill?  true}
     (for [years rows]
       [container {:key (str "year-row-" (str/join "-" years))
                   :layout :horizontally
                   :fill?  true
                   :gap?   false}
        (for [year years]
          [button {:key      (str "year-button-" year)
                   :fill?    true
                   :on-click #(on-click [(fmt/parse (fmt/formatter "yyyy-MM-dd") (str year "-01-01"))
                                         (time/last-day-of-the-month (fmt/parse (fmt/formatter "yyyy-MM-dd") (str year "-12-01")))])}
           (str year)])])]))


(defn months
  "Display the calendar-months of a [selected] year"
  [& args]
  (let [{:keys [params]}              (util/conform-or-fail ::months-args args)
        {:keys [every
                on-click
                selected]
         :or   {selected (time/now)}} params
        !model                        (atom selected)
        dt->str #(fmt/unparse (fmt/formatter "yyyyMMdd") %)]
    (fn [& args]
      (let [{:keys [params]}  (util/conform-or-fail ::months-args args)
            {:keys [every
                    selected]
             :or   {every 1
                    selected @!model}} params
            group-every       (if (= every 1) 3 every)
            year              (int (fmt/unparse (fmt/formatter "yyyy") @!model))
            rows              (->> (range 1 13)
                                   (map #(time/date-time year % 1))
                                   (partition group-every))]
        [container {:layout :vertically
                    :align  [:center :center]
                    :fill?  true}
         [calendar-nav {:jump 12 :format "yyyy" :!model !model}]
         (for [months rows]
           [container {:key (str/join months) :layout :horizontally :fill? true :gap? false}
            (if (= every 1)
              (for [month months]
                (let [val (fmt/unparse (fmt/formatter "MMMM") month)]
                  [button (merge {:key      (str "btn-" val)
                                  :fill?    true
                                  :on-click #(on-click [(time/first-day-of-the-month month) (time/last-day-of-the-month month)])}
                                 (when (= (dt->str selected) (dt->str (time/first-day-of-the-month month))) {:class "primary"})) val]))
              (let [val (str (fmt/unparse (fmt/formatter "MMMM") (first months)) " - " (fmt/unparse (fmt/formatter "MMMM") (last months)))]
                [button (merge {:key      (str "btn-" val)
                                :fill?    true
                                :on-click #(on-click [(time/first-day-of-the-month (first months)) (time/last-day-of-the-month (last months))])}
                               (when (= (dt->str selected) (dt->str (time/first-day-of-the-month (first months)))) {:class "primary"})) val]))])]))))


(defn days
  [& args]
  (let [{:keys [params]} (util/conform-or-fail ::days-args args)
        {:keys [start-of-week selected show-weekend?
                jump on-click nav? selectable? multiple?
                on-navigation class short-form?]
         :or   {start-of-week 1
                selected      (time/now)
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
                             (when selectable? "selectable")
                             (when (same-day? (date->day selected) day) "selected")
                             (when (same-day? (date->day (time/now)) day) "today")]]
                ^{:key (str "day-" (:day day))}
                [:td.Day {:class    (str/join " " classes)
                          :on-click #(when selectable? (on-click [dt dt]))}
                 [:span (:day day)]]))])]]])))
