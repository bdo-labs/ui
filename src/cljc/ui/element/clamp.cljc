(ns ui.element.clamp
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]
            [ui.util :as u]))


(re-frame/reg-event-db
 ::initialize-clamp
 (fn [db [k id coll]]
   (-> db
       (assoc-in [::collection id] (sort coll))
       (assoc-in [::lower-clamp id] 0)
       (assoc-in [::upper-clamp id] 100))))


(re-frame/reg-event-db
 ::set-active-knob
 (fn [db [_ id knob]]
   (assoc-in db [::active-knob id] knob)))


(re-frame/reg-event-db
 ::set-lower-clamp
 (fn [db [_ id percent]]
   (assoc-in db [::lower-clamp id] percent)))


(re-frame/reg-event-db
 ::set-upper-clamp
 (fn [db [_ id percent]]
   (assoc-in db [::upper-clamp id] percent)))


(re-frame/reg-event-db
 ::set-collection
 (fn [db [_ id coll]]
   (assoc-in db [::collection id] coll)))


(re-frame/reg-sub ::active-knob (fn [db [k id]] (or (get-in db [k id]) false)))
(re-frame/reg-sub ::lower-clamp (fn [db [k id]] (get-in db [k id])))
(re-frame/reg-sub ::upper-clamp (fn [db [k id]] (get-in db [k id])))
(re-frame/reg-sub ::collection (fn [db [k id]] (get-in db [k id])))


(re-frame/reg-sub
 ::lower-value
 (fn [[_ id]]
   [(re-frame/subscribe [::collection id])
    (re-frame/subscribe [::lower-clamp id])])
 (fn [[coll lower-clamp] _]
   (let [idx (Math/abs (Math/floor (* (/ (count coll) 100) lower-clamp)))]
     (nth coll idx))))


(re-frame/reg-sub
 ::upper-value
 (fn [[_ id]]
   [(re-frame/subscribe [::collection id])
    (re-frame/subscribe [::lower-clamp id])])
 (fn [[coll upper-clamp] _]
   (let [idx (Math/abs (Math/floor (* (/ (count coll) 100) upper-clamp)))]
     (nth coll 0))))


(defn clamp
  ([coll]
   (clamp {:range :both} coll))
  ([{:keys [labels? step]
     :or   {step 1}
     :as   params} coll]
   (let [id (u/gen-id)]
     (re-frame/dispatch [::initialize-clamp id coll])
     (let [active-knob       @(re-frame/subscribe [::active-knob id])
           lower-value       @(re-frame/subscribe [::lower-value id])
           upper-value       @(re-frame/subscribe [::upper-value id])
           lower-clamp       @(re-frame/subscribe [::lower-clamp id])
           upper-clamp       @(re-frame/subscribe [::upper-clamp id])
           extract-dimension (case (:range params)
                               :upper {:right 0
                                       :width (str (- 100 lower-clamp) "%")}
                               :both  {:left  (str lower-clamp "%")
                                       :width (str (- upper-clamp lower-clamp) "%")}
                               {:left  0
                                :width (str upper-clamp "%")})
           set-active-knob   #(re-frame/dispatch [::set-active-knob id %1])
           mouse-down        (fn [knob] #(set-active-knob knob))]
       [:div.Clamp
        (when labels?
          [:div.Label (str lower-value " - " upper-value)])
        [:div.Slider
         (when-not (= (:range params) :lower)
           [:div.Knob {:on-mouse-down (mouse-down :first)
                       :data-value    (str lower-value)
                       :style         {:left (str lower-clamp "%")}}])
         (when-not (= (:range params) :upper)
           [:div.Knob {:on-mouse-down (mouse-down :last)
                       :data-value    (str upper-value)
                       :style         {:left (str upper-clamp "%")}}])
         [:div.Extract
          {:style extract-dimension}]]]))))
