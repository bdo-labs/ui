(ns ui.docs.sheet
  (:require [clojure.test.check.generators :as gen]
            [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec :as spec]
            [clojure.string :as str]
            [ui.elements :as element]
            [ui.element.chooser :refer [chooser]]
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
(re-frame/reg-sub ::title-row util/extract)
(re-frame/reg-sub ::hide-column util/extract)


(re-frame/reg-sub
 ::content
 :<- [::title-row]
 :<- [::collection]
 (fn [[title-row coll]]
   (into [title-row] coll)))


(re-frame/reg-event-fx
 :init-sheet
 (fn [{:keys [db]} [k]]
   (let [rows        199
         body        (mapv vec (map first (drop 49 (spec/exercise ::fixture 199))))
         title-row   ["Segment" "Units-Sold" "Manufacturing" "Sales-Price" "Date"]
         hide-column (repeat (count title-row) false)
         uniqs       (->> body
                          (map first)
                          (distinct)
                          (map-indexed (fn [n text] {:id n :value text})))
         coll        (->> body
                          (map #(assoc-in % [0]
                                          (fn [{:keys [cell-ref]}]
                                            [chooser {:label      (first %)
                                                      :on-select  (fn [event] (let [value (:value (first event))]
                                                                               (re-frame/dispatch [:set-cell-val "Worksheet" cell-ref value])))
                                                      :searchable true
                                                      :items      uniqs}]))))]
     {:db (-> db
              (assoc ::title-row title-row)
              (assoc ::collection coll)
              (assoc ::hide-column hide-column))})))


(defn documentation []
  (let [content      @(re-frame/subscribe [::content])
        hide-columns @(re-frame/subscribe [::hide-column])]
    [layout/vertically
     [element/sheet {:name        "Worksheet"
                     :hide-column hide-columns}
      content]]))

