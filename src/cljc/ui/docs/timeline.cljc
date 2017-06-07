(ns ui.docs.timeline
  (:require [#?(:clj clj-time.format :cljs cljs-time.format) :as fmt]
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.util :as u]))


(re-frame/reg-event-db
 ::set-period
 (fn [db [_ period]] (assoc db ::period period)))


(re-frame/reg-sub ::period u/extract)


(defn timeline []
  [element/timeline
   {:min         2000
    :max         2100
    :show-years? false
    :on-change   #(re-frame/dispatch [::set-period %])}])


(defn documentation []
  (let [period @(re-frame/subscribe [::period])]
    [element/article
     "# Timeline"
     [timeline]
     (when (not-empty period)
       (let [from (:from period)
             to   (:to period)]
         [:small (str (fmt/unparse (fmt/formatter "dd. MMM. yyyy") from) " - "
                      (fmt/unparse (fmt/formatter "dd. MMM. yyyy") to))]))]))
