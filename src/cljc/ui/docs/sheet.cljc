(ns ui.docs.sheet
  (:require [clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [clojure.string :as str]
            [ui.elements :as element]
            [ui.layout :as layout]
            [tongue.core :as tongue]
            [ui.util :as u]
            #?(:cljs [reagent.core :refer [atom]])
            [re-frame.core :as re-frame]
            [ui.util :as util]))


(spec/def ::segment #{"Government" "Midmarket" "Enterprise" "Small Business" "Channel Partners"})
(spec/def ::units-sold (spec/and nat-int? #(> 99999 %) #(< 100 %)))
(spec/def ::manufacturing (spec/double-in :NaN? false :min 10 :max 200))
(spec/def ::sales-price (spec/double-in :NaN? false :min 100 :max 500))
(spec/def ::date (spec/inst-in #inst "2013" #inst "2017"))
(spec/def ::fixture
  (spec/cat :segment ::segment
            :units-sold ::units-sold
            :manufacturing ::manufacturing
            :sales-price ::sales-price
            :date ::date))


(defn format-number-no [n]
  (str/replace ((tongue/number-formatter {:group  " "
                                          :decimal "," }) n) #"(,..).+" "$1"))


(def inst-strings-en
  { :weekdays-narrow ["S" "M" "T" "W" "T" "F" "S"]
    :weekdays-short  ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"]
    :weekdays-long   ["Sunday" "Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday"]
    :months-narrow   ["J" "F" "M" "A" "M" "J" "J" "A" "S" "O" "N" "D"]
    :months-short    ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"]
    :months-long     ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"]
    :dayperiods      ["AM" "PM"]
    :eras-short      ["BC" "AD"]
    :eras-long       ["Before Christ" "Anno Domini"] })


(def format-inst
   (tongue/inst-formatter "{day}. {month-short}, {year}" inst-strings-en))


(re-frame/reg-sub ::collection util/extract)


(re-frame/reg-event-db
 :init-sheet
 (fn [db [k]]
   (let [rows             199
         body             (mapv vec (map first (drop 49 (spec/exercise ::fixture 199))))
         uniqs            (map-indexed #(do {:id %1 :text %2}) (distinct (mapv first body)))
         coll                (map #(assoc-in % [0] ^{:type       :string
                                                     :sort-value (first %)}
                                             [element/auto-complete {:placeholder (first %)
                                                                     :items       uniqs}]) body)]
     (assoc db ::collection coll))))


(defn documentation []
  (let [!caption?        (atom false)
        !column-heading? (atom false)
        !row-heading?    (atom false)
        title-row        ["Segment" "Units-Sold" "Manufacturing" "Sales-Price" "Date"]
        coll             (re-frame/subscribe [::collection])]
    (fn []
      [layout/vertically
       [layout/horizontally
        [element/checkbox {:checked? @!caption?
                           :on-click #(reset! !caption? (not @!caption?))} "Caption?"]
        [element/checkbox {:checked? @!column-heading?
                           :on-click #(reset! !column-heading? (not @!column-heading?))} "Column-Heading?"]
        [element/checkbox {:checked? @!row-heading?
                           :on-click #(reset! !row-heading? (not @!row-heading?))} "Row-Heading?"]]
       [element/sheet {:name             "Worksheet"
                       :caption?         @!caption?
                       :number-formatter format-number-no
                       :inst-formatter   format-inst
                       :locked           (vec (conj (repeat (dec (count title-row)) true) false))
                       :column-heading   (if @!column-heading? :alpha :hidden)
                       :row-heading      (if @!row-heading? :numeric :hidden)
                       :editable?        true}
        (into [title-row] @coll)]])))


;; (spec/def ::point nat-int?)
;; (spec/def ::volume (spec/and nat-int? #(< 10000 %) #(> 99999999 %)))
;; (spec/def ::title (spec/and string? #(> 20 (count %))))
;; (spec/def ::fixture
;;   (spec/cat :date (spec/inst-in #inst "2017" #inst "2020")
;;          :open ::point
;;          :high ::point
;;          :low ::point
;;          :close ::point
;;          :volume ::volume
;;          :title ::title))


