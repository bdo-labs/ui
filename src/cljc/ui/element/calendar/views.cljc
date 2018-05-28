(ns ui.element.calendar.views
  (:require [#?(:clj clj-time.coerce :cljs cljs-time.coerce) :as coerce]
            [#?(:clj clj-time.core :cljs cljs-time.core) :as time]
            [#?(:clj clj-time.format :cljs cljs-time.format) :as fmt]
            [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.string :as str]
            [re-frame.core :as re-frame]
            [ui.element.calendar.spec :as spec]
            [ui.element.button.views :refer [button]]
            [ui.element.icon.views :refer [icon]]
            [ui.element.containers.views :refer [container]]
            [ui.wire.polyglot :refer [translate]]
            [ui.util :as util]))

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
  [{:keys [id jump on-click format model]
    :or   {jump   1
           format :ui/year-month}
    :as   params}]
  (fn []
    (let [minimum?    #(after? % (:min params))
          maximum?    #(before? % (:max params))
          month       (time/months jump)
          current     (if-some [selected @model] selected [(time/now) (time/plus (time/now) month)])
          less        (time/minus (first current) month)
          more        (time/plus (first current) month)
          on-previous #(do (reset! model [less (time/minus (last current) month)])
                           (when (ifn? on-click) (on-click less %)))
          on-next     #(do (reset! model [more (time/plus (last current) month)])
                           (when (ifn? on-click) (on-click more %)))]
      [container {:layout :horizontally
                  :gap?   true
                  :fill?  true
                  :align  [:center :center]
                  :space  :between
                  :class  "calendar-nav"}
       ^{:key "previous-month"} [icon (merge {:class (str (when (minimum? (first current)) "disabled"))
                                              :font  "ion"}
                                             (when (not (minimum? (last current)))
                                               {:on-click on-previous})) "chevron-left"]
       ^{:key "current-month"} [:h3 (translate format (coerce/to-date (first current)))]
       ^{:key "next-month"} [icon (merge {:class (str (when (maximum? (last current)) "disabled"))
                                          :font  "ion"}
                                         (when (not (maximum? (last current)))
                                           {:on-click on-next})) "chevron-right"]])))

(defn years [& args]
  (let [{:keys [params]}   (util/conform! ::spec/years-args args)
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
                   :fill    true
                   :on-click #(on-click [(fmt/parse (fmt/formatter "yyyy-MM-dd") (str year "-01-01"))
                                         (time/last-day-of-the-month (fmt/parse (fmt/formatter "yyyy-MM-dd") (str year "-12-01")))])}
           (str year)])])]))

(defn months
  "Display the calendar-months of a year"
  [& args]
  (let [{:keys [params]} (util/conform! ::spec/months-args args)
        {:keys [on-click
                model]}  params
        model            (if (util/ratom? model) model (atom model))
        dt->str          #(fmt/unparse (fmt/formatter "yyyyMMdd") %)]
    (fn [& args]
      (let [{:keys [params]}  (util/conform! ::spec/months-args args)
            {:keys [every]
             :or   {every 1}} params
            minimum?          #(after? % (:min params))
            maximum?          #(before? % (:max params))
            group-every       (if (= every 1) 3 every)
            year              (int (fmt/unparse (fmt/formatter "yyyy") (if-some [y @model] (first y) (time/now))))
            rows              (->> (range 1 13)
                                 (map #(time/date-time year % 1))
                                 (partition group-every))]
        [container {:layout :vertically
                    :align  [:center :center]
                    :fill?  true}
         [calendar-nav (merge params {:jump 12 :format :ui/year :model model})]
         (doall
          (for [months rows]
            [container {:key (str/join months) :layout :horizontally :fill? true :gap? false}
             (if (= every 1)
               (for [month months]
                 (let [val (translate :ui/month (coerce/to-date month))]
                   [button (merge {:key      (str "btn-" val)
                                   :fill     true
                                   :disabled (or (minimum? (time/first-day-of-the-month month))
                                                 (maximum? (time/last-day-of-the-month month)))
                                   :on-click #(let [selected [(time/first-day-of-the-month month)
                                                              (time/last-day-of-the-month month)]]
                                                (reset! model selected)
                                                (when (ifn? on-click) (on-click selected %)))}
                                  (when-some [selected @model]
                                    (when (and (= (dt->str (first selected))
                                                  (dt->str (time/first-day-of-the-month month)))
                                               (= (dt->str (last selected))
                                                  (dt->str (time/last-day-of-the-month month))))
                                      {:class "primary"}))) val]))
               (let [val (str (translate :ui/month (coerce/to-date (first months)))
                              " - "
                              (translate :ui/month (coerce/to-date (last months))))]
                 [button (merge {:key      (str "btn-" val)
                                 :fill     true
                                 :disabled (or (minimum? (time/first-day-of-the-month (first months)))
                                               (maximum? (time/last-day-of-the-month (last months))))
                                 :on-click #(let [selected [(time/first-day-of-the-month (first months))
                                                            (time/last-day-of-the-month (last months))]]
                                              (reset! model selected)
                                              (when (ifn? on-click) (on-click selected %)))}
                                (when-some [selected @model]
                                  (when (and (= (dt->str (first selected))
                                                (dt->str (time/first-day-of-the-month (first months))))
                                             (= (dt->str (last selected))
                                                (dt->str (time/last-day-of-the-month (last months)))))
                                    {:class "primary"}))) val]))]))]))))

(defn days
  [& args]
  (let [{:keys [params]}          (util/conform! ::spec/days-args args)
        {:keys [start-of-week show-weekend?
                jump on-click nav? selectable? multiple?
                on-navigation class short-form?
                model period-picker]
         :or   {start-of-week 1
                jump          1
                short-form?   false
                nav?          true
                show-weekend? true
                selectable?   true
                period-picker false}} params
        select*                   (atom 0)
        model                     (if (util/ratom? model) model (atom model))
        num-days                  (if (true? show-weekend?) 7 5)
        weekdays-num              (range start-of-week (+ start-of-week num-days))]
    (fn []
      (let [minimum? #(after? % (:min params))
            maximum? #(before? % (:max params))
            caret    (if-some [selected @model] selected [(time/today-at 0 0 0 1) (time/today-at-midnight)])]
        [:div.Calendar
         (when nav?
           [calendar-nav (merge params
                                {:model    model
                                 :on-click on-navigation})])
         [:table {:class class}
          [:thead.Weekdays
           [:tr
            (doall
             (for [weekday weekdays-num]
               (let [weekday-name (translate (if short-form? :ui/weekday-short :ui/weekday-long)
                                             (coerce/to-date (str "2016-1-" (+ 3 weekday))))]
                 ^{:key (str "weekday-" weekday)}
                 [:th weekday-name])))]]
          [:tbody
           (for [week (weeks (first caret))]
             ^{:key (str "week-" (:month (first week)) "-" (:day (first week)))}
             [:tr.Week
              (for [weekday weekdays-num]
                (let [day     (nth week (dec weekday))
                      dt      (day->date day)
                      classes [(case (:belongs-to-month day) :previous "Previous" :next "Next" "")
                               (when selectable? "selectable")
                               (when (or (maximum? dt)
                                         (minimum? dt)) "disabled")
                               (when (and period-picker
                                          (after? (first caret) dt)
                                          (before? (last caret) dt)) "between")
                               (when (or (same-day? (date->day (first caret)) day)
                                         (same-day? (date->day (last caret)) day)) "selected")
                               (when (same-day? (date->day (time/now)) day) "today")]]
                  ^{:key (str "day-" (:day day))}
                  [:td.Day {:class    (str/join " " classes)
                            :on-click #(when selectable?
                                         (swap! select* inc)
                                         (if (or (not period-picker)
                                                 (odd? @select*))
                                           (reset! model [dt dt])
                                           (if (after? (first @model) dt)
                                             (reset! model [(first @model) dt])
                                             (reset! model [dt (first @model)])))
                                         (when (ifn? on-click) (on-click @model %)))}
                   [:span (:day day)]]))])]]]))))
