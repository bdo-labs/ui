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
  [:.Clamp {:box-sizing :border-box
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
    [:&:hover {:background primary
               :transform  [[(translateY (unit/percent -50)) (scale 1.4)]]}
     [:&:after {:opacity 1}]]
    [:&:after {:display       :block
               :background    secondary
               :color         (if (dark? (vals (select-keys secondary [:red :green :blue])))
                                (color/darken "#fff" 5) (color/lighten "#000" 5))
               :border-radius (unit/em 0.2)
               :padding       [[(unit/em 0.25) (unit/em 0.5)]]
               :transition    [[:200ms :ease]]
               :opacity       0
               :left          (unit/percent 50)
               :font-size     (unit/em 0.7)
               :transform     (translateX (unit/percent -50))
               :position      :absolute
               :bottom        (unit/em 1.5)
               :content       (attr :data-value)}]]])


(re-frame/reg-event-db
 ::initialize-clamp
 (fn [db [k id coll]]
   (-> db
       (assoc-in [::collection id] (sort coll)))))


(re-frame/reg-event-db
 ::set-active-knob
 (fn [db [_ id knob]]
   (assoc-in db [::active-knob id] knob)))


(re-frame/reg-event-db
 ::move-knob
 (fn [db [_ id pct]]
   (let [percent (Math/abs (Math/floor pct))]
     (assoc-in db [::lower-clamp id] percent))))


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


(re-frame/reg-sub ::active-knob (fn [db [k id]] (get-in db [k id])))
(re-frame/reg-sub ::lower-clamp (fn [db [k id]] (get-in db [k id])))
(re-frame/reg-sub ::upper-clamp (fn [db [k id]] (get-in db [k id])))
(re-frame/reg-sub ::collection (fn [db [k id]] (get-in db [k id])))


;; FIXME This only works because the collection is equal to the percentage-count
(re-frame/reg-sub
 ::lower-value
 (fn [[_ id]]
   [(re-frame/subscribe [::collection id])
    (re-frame/subscribe [::minimum id])
    (re-frame/subscribe [::lower-clamp id])])
 (fn [[coll minimum lower-clamp] _]
   (or (Math/abs (Math/floor (* (/ (count coll) 100) lower-clamp))) minimum)))


(re-frame/reg-sub
 ::upper-value
 (fn [[_ id]]
   [(re-frame/subscribe [::collection id])
    (re-frame/subscribe [::maximum id])
    (re-frame/subscribe [::lower-clamp id])])
 (fn [[coll maximum upper-clamp] _]
   (let [idx (Math/abs (Math/floor (* (/ (count coll) 100) upper-clamp)))]
     (or (nth coll idx) maximum))))


(re-frame/reg-sub
 ::minimum
 (fn [[_ id]]
   [(re-frame/subscribe [::collection id])])
 (fn [[coll] _]
   (->> coll
        (sort)
        (first))))


(re-frame/reg-sub
 ::maximum
 (fn [[_ id]]
   [(re-frame/subscribe [::collection id])])
 (fn [[coll] _]
   (->> coll
        (sort)
        (last))))


(defn clamp
  ([coll]
   (clamp {:range :both} coll))
  ([{:keys [id labels? step on-change]
     :or   {step 1}
     :as   params} coll]
   (re-frame/dispatch [::initialize-clamp id coll])
   (let [;; Subscriptions
         active-knob       @(re-frame/subscribe [::active-knob id])
         lower-value       @(re-frame/subscribe [::lower-value id])
         upper-value       @(re-frame/subscribe [::upper-value id])
         lower-clamp       @(re-frame/subscribe [::lower-clamp id])
         upper-clamp       @(re-frame/subscribe [::upper-clamp id])
         ;; Events
         set-active-knob   #(re-frame/dispatch [::set-active-knob id %1])
         mouse-within      #(when-not (nil? active-knob)
                              (let [x (.-mousePercentX %)]
                                (re-frame/dispatch [::move-knob id x])))
         mouse-up          (fn [] #(set-active-knob nil))
         mouse-down        (fn [knob] #(set-active-knob knob))
         ;; Transformations
         extract-dimension (case (:range params)
                             :upper {:left  (str lower-clamp "%")
                                     :right 0}
                             :both  {:left  (str lower-clamp "%")
                                     :width (str (- upper-clamp lower-clamp) "%")}
                             {:left  0
                              :width (str upper-clamp "%")})]
     (on-change {:lower lower-value
                 :upper upper-value})
     [:div.Clamp {:id  id
                  :key id}
      (when labels?
        [:div.Label (case (:range params)
                      :upper lower-value
                      :lower upper-value
                      (str lower-value " - " upper-value))])
      ;; Wrap the slider in a boundary that exposes a relative clientX-stream
      [boundary {:on-mouse-within mouse-within
                 :on-mouse-up     mouse-up
                 :offset          {:top  (unit/rem 1)
                                   :left (unit/rem 3)}}
       [:div.Slider {}
        (when-not (= (:range params) :lower)
          [:div.Knob {:on-mouse-down (mouse-down :first)
                      :data-value    (str lower-value)
                      :style         {:left (str lower-clamp "%")}}])
        (when-not (= (:range params) :upper)
          [:div.Knob {:on-mouse-down (mouse-down :last)
                      :data-value    (str upper-value)
                      :style         {:left (str upper-clamp "%")}}])
        [:div.Extract
         {:style extract-dimension}]]]])))
