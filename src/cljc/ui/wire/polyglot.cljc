(ns ui.wire.polyglot
  (:require [tongue.core :as tongue]
            [re-frame.core :as re-frame]
            [clojure.string :as str]
            [ui.util :as util]))

(def inst-strings-en
  {:weekdays-narrow ["S" "M" "T" "W" "T" "F" "S"]
   :weekdays-short  ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"]
   :weekdays-long   ["Sunday" "Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday"]
   :months-narrow   ["J" "F" "M" "A" "M" "J" "J" "A" "S" "O" "N" "D"]
   :months-short    ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"]
   :months-long     ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"]
   :dayperiods      ["AM" "PM"]})

(def format-inst
  (tongue/inst-formatter "{month-short} {day}, {year} {hour24-padded}.{minutes-padded}" inst-strings-en))

(def format-number-en
  (tongue/number-formatter {:group ","
                            :decimal "."}))

(def en
  {:tongue/format-inst      format-inst
   :tongue/format-number    format-number-en
   :ui/time-ago-plural      (fn [s] (str s "s"))
   :ui/date-full            (tongue/inst-formatter "{month-long} {day}, {year}" inst-strings-en)
   :ui/date-short           (tongue/inst-formatter "{month-numeric}/{day}/{year-2digit}" inst-strings-en)
   :ui/year-month           (tongue/inst-formatter "{month-long} {year}" inst-strings-en)
   :ui/year                 (tongue/inst-formatter "{year}" inst-strings-en)
   :ui/month                (tongue/inst-formatter "{month-long}" inst-strings-en)
   :ui/weekday-long         (tongue/inst-formatter "{weekday-long}" inst-strings-en)
   :ui/weekday-short        (tongue/inst-formatter "{weekday-short}" inst-strings-en)
   :ui/sort-ascending       "Sort Ascending"
   :ui/sort-descending      "Sort Descending"
   :ui/optional             "Optional"
   :ui/required             "Required"
   :ui/show-rows-containing "Show only rows containing:"
   :ui/time-period          "Time-period"
   :ui/date-period          "Date-period"
   :ui/change-log-version   "{1} ({2})"
   :ui/report-issue         "Report and issue"
   :ui/keyword              "Keyword"
   :ui/hello                "hello {1}!"})

;; Events -----------------------------------------------------------------


(re-frame/reg-event-db
 :ui/set-current-language
 (fn [db [k lang]]
   (assoc db (util/->ref-name k) lang)))

(re-frame/reg-event-db
 :ui/add-dictionary
 (fn [db [_ lang dictionary]]
   (assoc-in db [:ui/dictionaries lang] dictionary)))

(re-frame/reg-event-fx
 :init-polyglot
 (fn [{:keys [db]} _]
   {:dispatch [:ui/add-dictionary :en en]
    :db db}))

;; Subscriptions ----------------------------------------------------------


(re-frame/reg-sub
 :ui/current-language
 (fn [db [k]]
   (get db k :en)))

(re-frame/reg-sub :ui/dictionaries util/extract)

(re-frame/reg-sub
 :ui/translate
 :<- [:ui/dictionaries]
 :<- [:ui/current-language]
 (fn [[dictionaries current-language] [_ & phrase]]
   (let [translate  (tongue/build-translate dictionaries)
         translated (str (apply translate (into [current-language] phrase)))]
     (when (str/starts-with? translated (str "{Missing key :"))
       (re-frame/dispatch [:missing-translation phrase]))
     translated)))

(defn translate [& args]
  (let [phrase (into [:ui/translate] args)]
    @(re-frame/subscribe phrase)))
