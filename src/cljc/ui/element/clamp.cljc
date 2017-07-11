(ns ui.element.clamp
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [clojure.string :as str]
            [garden.units :as unit]
            [garden.color :as color]
            [re-frame.core :as re-frame]
            [ui.element.boundary :refer [boundary]]
            [ui.util :as u]))


(defcssfn attr)
(defcssfn scale)
(defcssfn translateX)
(defcssfn translateY)
(defcssfn translateZ)


(defn dark?
  "Is the [r g b]-color supplied a dark color?"
  [[r g b]]
  (> (- 1 (/ (+ (* 0.299 r)
                (* 0.587 g)
                (* 0.114 b)) 255)) 0.5))


(defn style
  [{:keys [primary secondary]}]
  [[:.Clamp {:box-sizing :border-box
              :padding    (unit/rem 1)
              :width      (unit/percent 100)
              :text-align :center
              :transform  (translateZ 0)
              :position   :relative}
     [:.Label {:text-align    :left
               :font-weight   :normal
               :margin-bottom (unit/rem 2)
               :overflow      :hidden
               :white-space   :nowrap
               :text-overflow :ellipsis
               :font-size     (unit/em 1.2)}]
     [:.Slider {:height     (unit/px 2)
                :background :silver}]
     [:.Extract {:background primary
                 :position   :absolute
                 :height     (unit/percent 100)}]
     [:.Knob {:background       :white
              :border-radius    (unit/percent 50)
              :border           [[:solid (unit/px 2) primary]]
              :position         :absolute
              :transform-origin [[:center :center]]
              :transition       [[:200ms :ease]]
              :transform        [[(translateY (unit/percent -45)) (translateZ 0)]]
              :cursor           :pointer
              :height           (unit/rem 0.5)
              :width            (unit/rem 0.5)
              :z-index          10}
      [#{:&:hover :&.Dirty} {:background primary}]
      [:&:hover {:transform [[(translateY (unit/percent -50)) (scale 1.4)]]}
       [:&:after {:opacity 1}]]
      [:&:after {:display       :block
                 :background    secondary
                 ;; :color         (if (dark? (vals (select-keys secondary [:red :green :blue])))
                 ;;                  (color/darken "#fff" 5) (color/lighten "#000" 5))
                 :border-radius (unit/em 0.2)
                 :padding       [[(unit/em 0.25) (unit/em 0.5)]]
                 :transition    [[:200ms :ease]]
                 :opacity       0
                 :left          (unit/percent 50)
                 :font-size     (unit/em 0.7)
                 :transform     (translateX (unit/percent -50))
                 :position      :absolute
                 :bottom        (unit/em 1.5)
                 :content       (attr :data-value)}]]]])


;; Events


(re-frame/reg-event-db
 ::initialize-clamp
 (fn [db [_ id coll]]
   (-> db
       (assoc-in [::collection id] (sort coll)))))


(re-frame/reg-event-db
 ::set-active-knob
 (fn [db [_ id knob]]
   (assoc-in db [::active-knob id] knob)))


(re-frame/reg-event-db
 ::unset-active-knob
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


;; Subscriptions

(re-frame/reg-sub ::active-knob (fn [db [k id]] (get-in db [k id])))


(re-frame/reg-sub
 ::lower-knob
 (fn [db [k id]]
   (get-in db [k id] 0)))


(re-frame/reg-sub
 ::upper-knob
 (fn [db [k id]] (get-in db [k id] 100)))


(re-frame/reg-sub ::collection (fn [db [k id]] (get-in db [k id])))


(re-frame/reg-sub
 ::lower-value
 (fn [[_ id]]
   [(re-frame/subscribe [::collection id])
    (re-frame/subscribe [::lower-knob id])])
 (fn [[coll lower-knob] _]
   (let [idx (->> (/ (count coll) 100)
                  (* lower-knob)
                  (Math/round)
                  (Math/abs)
                  (max 0))]
     (nth coll idx))))


(re-frame/reg-sub
 ::upper-value
 (fn [[_ id]]
   [(re-frame/subscribe [::collection id])
    (re-frame/subscribe [::upper-knob id])])
 (fn [[coll upper-knob] _]
   (let [idx (->> (* (/ (dec (count coll)) 100) upper-knob)
                  (Math/round)
                  (Math/abs)
                  (min (dec (count coll))))]
     (nth coll idx))))


(defn clamp
  ([coll]
   (clamp {:range :both} coll))
  ([{:keys [id labels? step on-change]
     :or   {step 1}
     :as   params} coll]
   (re-frame/dispatch [::initialize-clamp id coll])
   (let [;; Subscriptions
         active-knob     @(re-frame/subscribe [::active-knob id])
         lower-value     @(re-frame/subscribe [::lower-value id])
         upper-value     @(re-frame/subscribe [::upper-value id])
         lower-knob      @(re-frame/subscribe [::lower-knob id])
         upper-knob      @(re-frame/subscribe [::upper-knob id])
         ;; Events
         set-active-knob #(re-frame/dispatch [::set-active-knob id %])
         mouse-within    #(when-not (nil? active-knob)
                            (let [x (.-mousePercentX %)]
                              (re-frame/dispatch [::move-knob id x])
                              (on-change {:min lower-value
                                          :max upper-value})))
         mouse-up        #(re-frame/dispatch [::unset-active-knob id])
         mouse-down      (fn [knob] #(set-active-knob knob))]
     [:div.Clamp {:id  id
                  :key id}
      (when labels?
        [:div.Label (case (:range params)
                      :upper upper-value
                      :lower lower-value
                      (str lower-value " - " upper-value))])
      ;; Wrap the slider in a boundary that exposes a relative clientX-stream
      [boundary {:on-mouse-within mouse-within
                 :on-mouse-up     mouse-up
                 :offset          {:top  (unit/rem 1)
                                   :left (unit/rem 3)}}
       [:div.Slider {}
        (when-not (= (:range params) :lower)
          [:div.Knob {:on-mouse-down (mouse-down ::lower-knob)
                      :data-value    (str lower-value)
                      :class         (if (not= lower-knob 0) "Dirty" "")
                      :style         {:left (str lower-knob "%")}}])
        (when-not (= (:range params) :upper)
          [:div.Knob {:on-mouse-down (mouse-down ::upper-knob)
                      :class         (if (not= upper-knob 100) "Dirty" "")
                      :data-value    (str upper-value)
                      :style         {:left (str upper-knob "%")}}])
        [:div.Extract
         {:style (case (:range params)
                   :upper {:left  (str lower-knob "%")
                           :right 0}
                   :both  {:left  (str lower-knob "%")
                           :width (str (- upper-knob lower-knob) "%")}
                   {:left  0
                    :width (str upper-knob "%")})}]]]])))
