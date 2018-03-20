(ns ui.element.clamp.views
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]))


;; Events -----------------------------------------------------------------


(re-frame/reg-event-db ::initialize-clamp
                       (fn [db [_ id coll]]
                         (-> db
                             (assoc-in [::collection id] (sort coll)))))


(re-frame/reg-event-db ::set-active-knob
                       (fn [db [_ id knob]]
                         (assoc-in db [::active-knob id] knob)))


(re-frame/reg-event-db ::unset-active-knob
                       (fn [db [_ id]]
                         (update-in db [::active-knob] dissoc id)))


(re-frame/reg-event-fx
  ::move-knob
  (fn [{:keys [db]} [_ id pct]]
    (let [active-knob (get-in db [::active-knob id])
          percent (Math/abs (Math/floor pct))]
      (if (= active-knob ::lower-knob)
        (let [maximum (get-in db [::upper-knob id] 100)]
          {:db (assoc-in db [active-knob id] (min maximum percent))})
        (let [minimum (get-in db [::lower-knob id] 0)]
          {:db (assoc-in db [active-knob id] (max minimum percent))})))))


;; Subscriptions ----------------------------------------------------------


(re-frame/reg-sub ::active-knob (fn [db [k id]] (get-in db [k id])))


(re-frame/reg-sub ::lower-knob (fn [db [k id]] (get-in db [k id] 0)))


(re-frame/reg-sub ::upper-knob (fn [db [k id]] (get-in db [k id] 100)))


(re-frame/reg-sub ::collection (fn [db [k id]] (get-in db [k id])))


(re-frame/reg-sub ::lower-value
                  (fn [[_ id]] [(re-frame/subscribe [::collection id])
                                (re-frame/subscribe [::lower-knob id])])
                  (fn [[coll lower-knob] _]
                    (let [idx (->> (/ (count coll) 100)
                                   (* lower-knob)
                                   (Math/round)
                                   (Math/abs)
                                   (max 0))]
                      (nth coll idx))))


(re-frame/reg-sub ::upper-value
                  (fn [[_ id]] [(re-frame/subscribe [::collection id])
                                (re-frame/subscribe [::upper-knob id])])
                  (fn [[coll upper-knob] _]
                    (let [idx (->> (* (/ (dec (count coll)) 100) upper-knob)
                                   (Math/round)
                                   (Math/abs)
                                   (min (dec (count coll))))]
                      (nth coll idx))))


;; Views ------------------------------------------------------------------


(defn clamp
  ([coll] (clamp {:range :both} coll))
  ([{:keys [id labels? step on-change], :or {step 1}, :as params} coll]
   (re-frame/dispatch [::initialize-clamp id coll])
   (let [;; Subscriptions
         active-knob @(re-frame/subscribe [::active-knob id])
         lower-value @(re-frame/subscribe [::lower-value id])
         upper-value @(re-frame/subscribe [::upper-value id])
         lower-knob @(re-frame/subscribe [::lower-knob id])
         upper-knob @(re-frame/subscribe [::upper-knob id])
         ;; Events
         set-active-knob #(re-frame/dispatch [::set-active-knob id %])
         mouse-within #(when-not (nil? active-knob)
                        (let [x (.-mousePercentX %)]
                          (re-frame/dispatch [::move-knob id x])
                          (on-change {:min lower-value, :max upper-value})))
         mouse-up #(re-frame/dispatch [::unset-active-knob id])
         mouse-down (fn [knob] #(set-active-knob knob))]
     [:div.Clamp {:id id, :key id}
      (when labels?
        [:div.Label
         (case (:range params)
           :upper upper-value
           :lower lower-value
           (str lower-value " - " upper-value))]) ;; Wrap the slider in a
                                                  ;; boundary that exposes a
                                                  ;; relative clientX-stream
      #_[boundary {:offset [(unit/rem 1) (unit/rem 3)]}
         [:div.Slider {}
          (when-not (= (:range params) :lower)
            [:div.Knob
             {:on-mouse-down (mouse-down ::lower-knob),
              :class (if (not= lower-knob 0) "Dirty" ""),
              :data-value (str lower-value),
              :style {:left (str lower-knob "%")}}])
          (when-not (= (:range params) :upper)
            [:div.Knob
             {:on-mouse-down (mouse-down ::upper-knob),
              :class (if (not= upper-knob 100) "Dirty" ""),
              :data-value (str upper-value),
              :style {:left (str upper-knob "%")}}])
          [:div.Extract
           {:style (case (:range params)
                     :upper {:left (str lower-knob "%"), :right 0}
                     :both {:left (str lower-knob "%"),
                            :width (str (- upper-knob lower-knob) "%")}
                     {:left 0, :width (str upper-knob "%")})}]]]])))

