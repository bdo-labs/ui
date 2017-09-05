(ns ui.docs.sheet
  (:require [clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [clojure.string :as str]
            [ui.elements :as element]
            [ui.element.auto-complete :refer [auto-complete]]
            [ui.layout :as layout]
            [tongue.core :as tongue]
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


(re-frame/reg-sub ::collection util/extract)


(re-frame/reg-event-fx
 :init-sheet
 (fn [{:keys [db]} [k]]
   (let [rows  199
         body  (mapv vec (map first (drop 49 (spec/exercise ::fixture 199))))
         ;; uniqs (->> body
         ;;            (map first)
         ;;            (distinct)
         ;;            (map-indexed (fn [n text] {:id n :text text})))
         ;; coll (->> body
         ;;           (map #(assoc-in % [0] [auto-complete {:placeholder (first %)
         ;;                                                 :items       uniqs}])))
         ]
     {:db (assoc db ::collection body)})))


(defn documentation []
  (let [title-row       ["Segment" "Units-Sold" "Manufacturing" "Sales-Price" "Date"]
        coll            @(re-frame/subscribe [::collection])
        content         (into [title-row] coll)]
    [layout/vertically
     [element/sheet {:name "Worksheet"} content]]))

