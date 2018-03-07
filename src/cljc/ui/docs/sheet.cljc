(ns ui.docs.sheet
  (:require [clojure.test.check.generators :as gen]
            [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [ui.elements :as element]
            [ui.element.chooser :refer [chooser]]
            [ui.layout :as layout]
            [tongue.core :as tongue]
            [re-frame.core :as re-frame]
            [ui.util :as util]
            [clojure.set :as set]))

(spec/def ::segment #{"Government" "Midmarket" "Enterprise" "Small Business" "Channel Partners"})
(spec/def ::units-sold (spec/and nat-int? #(> 99999 %) #(< 100 %)))
(spec/def ::manufacturing (spec/double-in :NaN? false :min 10 :max 200))
(spec/def ::sales-price (spec/double-in :NaN? false :min 100 :max 500))
(spec/def ::date (spec/inst-in #inst "2013" #inst "2017"))
(spec/def ::fixture
  (spec/cat :units-sold ::units-sold
            :segment ::segment
            :manufacturing ::manufacturing
            :sales-price ::sales-price
            :date ::date))

(re-frame/reg-sub ::collection util/extract)
(re-frame/reg-sub ::title-row util/extract)

(re-frame/reg-sub
 ::content
 :<- [::title-row]
 :<- [::collection]
 (fn [[title-row coll]]
   (into [title-row] coll)))

(re-frame/reg-event-fx
 :init-sheet
 (fn [{:keys [db]} [k]]
   (let [num-items 200
         items     #{{:id 0 :value "Government"} {:id 1 :value "Midmarket"} {:id 2 :value "Enterprise"} {:id 3 :value "Small Business"} {:id 4 :value "Channel Partners"}}
         body      (mapv vec (map first (drop 49 (spec/exercise ::fixture (+ num-items 49)))))
         title-row ["Units-Sold" "Segment" "Manufacturing" "Sales-Price" "Date"]
         coll      (->> body
                        (map #(let [value (second %)]
                                (assoc-in % [1] {:display-value value
                                                 :value         value
                                                 :items         items
                                                 :editable?     true
                                                 :on-blur (fn [event]
                                                            (util/log (.-value (.-target event))))
                                                 :on-select     (fn [row cell-ref event]
                                                                  (let [{:keys [id value]} (first event)]
                                                                    (util/log row cell-ref id value)))}))))]
     {:db (-> db
              (assoc ::title-row title-row)
              (assoc ::collection coll))})))

(defn documentation []
  (let [content  @(re-frame/subscribe [::content])
        segments (mapv (fn [segment] [{:value      segment
                                       :title-row? false}]) (spec/describe ::segment))]
    [layout/vertically {:rounded? true
                        :raised? true
                        :background :white
                        :style {:margin "2em"}
                        :fill? true}
     [:h1 "sheet"]
     [element/sheet {:name           "Segments"
                     :hidden         true}
      segments]
     [element/sheet {:name           "Worksheet"
                     :column-widths [100 500 100]
                     :hide-columns #{:C}
                     :editable?      false}
      content]]))
