(ns ui.docs.sheet
  (:require [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [ui.element.auto-complete :refer [auto-complete]]
            [ui.elements :as element]
            [ui.layout :as layout]
            [tongue.core :as tongue]))


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


(defn documentation []
  (let [rows      199
        title-row ["Segment" "Units-Sold" "Manufacturing" "Sales-Price" "Date"]
        body      (mapv vec (map first (drop 49 (spec/exercise ::fixture (+ rows 50)))))
        ;; uniqs     (map-indexed #(do {:id %1 :text %2}) (distinct (mapv first body)))
        data      (into [title-row] body
                        #_(->> body
                             (map (fn [row]
                                    (let [first-col [auto-complete {:placeholder (first row)
                                                                    :items       uniqs}]]
                                      (assoc-in row [0] first-col))))))]
    [layout/vertically {:fill true}
     [element/sheet {:name             "Worksheet"
                     :caption?         true
                     :number-formatter format-number-no
                     :inst-formatter   format-inst
                     :column-heading   :alpha
                     :row-heading      :numeric
                     :editable?        true
                     } data]]))


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

