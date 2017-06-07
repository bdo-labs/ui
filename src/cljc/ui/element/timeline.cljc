(ns ui.element.timeline
  (:require [#?(:clj clj-time.core :cljs cljs-time.core) :as t]
            [clojure.string :as str]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [ui.element.button :refer [button]]
            [ui.element.containers :refer [container]]
            [ui.util :as u]
            [clojure.spec.alpha :as spec]))


(def months
  ["January"
   "February"
   "March"
   "April"
   "May"
   "June"
   "July"
   "August"
   "September"
   "October"
   "November"
   "December"])


(defn previous-month [month]
  (->> (map-indexed #(when (= (subs %2 0 3) (subs month 0 3))
                       (if (> %1 0)
                         (nth months (dec %1))
                         (nth months 11))) months)
       (remove empty?)
       (first)))


(defn next-month [month]
  (->> (map-indexed #(when (= (subs %2 0 3) (subs month 0 3))
                       (if (< %1 11)
                         (nth months (inc %1))
                         (nth months 0))) months)
       (remove empty?)
       (first)))


(def quarters ["Q1" "Q2" "Q3" "Q4"])


(defn year-picker
  [params]
  (let [minimum (or (:min params) 1900)
        maximum (or (:max params) 2100)
        years (->> (range minimum maximum)
                   (mapv (fn [year] [year (->> (take 12 months)
                                              (mapv #(subs % 0 3))
                                              (partition 6))])))]
    [:table.Year-picker
     (for [[year rows] years]
       [:tbody
        (for [n (range (count rows))]
          [:tr
           (when (even? n) [:td {:row-span 2} year])
           (for [month (nth rows n)]
             [:td month])])])]))


;; REVIEW Apparently `indexOf` is part of Clojure, make sure they are
;; functionally equal before swapping
(defn index-of [coll value]
  (some (fn [[idx item]] (if (= value item) idx))
        (map-indexed vector coll)))


(re-frame/reg-event-fx
 ::set-year
 [re-frame/trim-v]
 (fn [{:keys [db]} [id year]]
   {::execute-set-period {:id id :year year :months (vec (map-indexed (fn [n mnth] (inc n)) months))}
    :db (assoc-in db [::timeline id] {:year year})}))


(re-frame/reg-event-fx
 ::set-quarter
 [re-frame/trim-v]
 (fn [{:keys [db]} [id year quarter]]
   (let [mnths (-> (partition 3 months)
                   (nth (index-of quarters quarter)))]
     {::execute-set-period {:id id :year year :months (map #(inc (index-of months %)) mnths)}
      :db                  (assoc-in db [::timeline id] {:year    year
                                                         :quarter quarter})})))


(re-frame/reg-event-fx
 ::set-months
 [re-frame/trim-v]
 (fn [{:keys [db]} [id year mnths]]
   {::execute-set-period {:id id :year year :months (map #(inc (index-of months %)) mnths)}
    :db                  (assoc-in db [::timeline id] {:year   year
                                                       :months mnths})}))


(re-frame/reg-fx
 ::execute-set-period
 (fn [{:keys [id year] :as params}]
   (let [mnths (:months params)
         from (t/first-day-of-the-month (t/date-time year (first mnths)))
         to (t/last-day-of-the-month (t/date-time year (last mnths)))]
     (re-frame/dispatch [::set-period id from to]))))


(re-frame/reg-event-db
 ::set-period
 [re-frame/trim-v]
 (fn [db [id from to]]
   (assoc-in db [::period id] {:from from
                              :to to})))


(re-frame/reg-sub
 ::timeline
 (fn [db [_ id]]
   (get-in db [::timeline id])))


(re-frame/reg-sub
 ::period
 (fn [db [_ id]]
   (get-in db [::period id])))


(defn transform [x]
  {:WebkitTransform (str "translateX(" x "px)")
   :MozTransform (str "translateX(" x "px)")
   :MsTransform (str "translateX(" x "px)")
   :transform (str "translateX(" x "px)")})


(defn visible-period [current]
  (range (- current 3) (+ current 3)))


;; (spec/def ::position inst?)
(spec/def ::show-year? boolean?)
(spec/def ::show-tertial? boolean?)
(spec/def ::show-quarter? boolean?)
(spec/def ::show-twomonth? boolean?)
(spec/def ::show-month? boolean?)


(spec/def ::timeline-args
  (spec/keys :opt-un [
                   ;; ::position
                   ::show-year?
                   ::show-tertial?
                   ::show-quarter?
                   ::show-twomonth?
                   ::show-month?]))


(defn timeline
  "# Timeline
   Quickly choose a date-range within a year by touch-dragging
  TODO Replace reagent/create-class with a callback-reference
  "
  [{:keys [position on-change show-years? show-quarters? show-months? show-twomonths? show-tertial?]
              :or   {position        (int (t/year (t/now)))
                     show-years?     true
                     show-quarters?  true
                     show-months?    true
                     show-twomonths? true
                     show-tertial?   true}
              :as   params}]
  (when-let [id (u/gen-id)]
    (let [!hammer-manager (atom nil)
          !pan            (atom 0)
          !start-pan      (atom false)
          !caret          (atom position)
          minimum         (or (:min params) (- position 100))
          maximum         (or (:max params) (+ position 100))
          timeline        (re-frame/subscribe [::timeline id])
          period          (re-frame/subscribe [::period id])
          click-month     (fn [year month] (re-frame/dispatch [::set-months id year month]))
          click-quarter   (fn [year quarter] (re-frame/dispatch [::set-quarter id year quarter]))
          click-year      (fn [year] (re-frame/dispatch [::set-year id year]))]
      #?(:cljs (reagent/create-class
                {:display-name "timeline"
                 :component-did-mount (fn [this]
                                        (let [el    (reagent/dom-node this)
                                              mc    (new js/Hammer.Manager el)
                                              height (js/parseInt (.-height (js/getComputedStyle el.firstElementChild)))
                                              width (int (/ (js/parseInt (.-width (js/getComputedStyle el.firstElementChild)))
                                                            (count (visible-period @!caret))))]
                                          (set! (.-height (.-style el)) (str height "px"))
                                          ;; Set starting position to `position`
                                          (when (false? @!start-pan)
                                            (let [pan-pos (* (index-of (visible-period @!caret) position) width -1)]
                                              (reset! !pan pan-pos)
                                              (reset! !start-pan pan-pos)))
                                          ;; Panning
                                          (js-invoke mc "add" (new js/Hammer.Pan #js{"direction" js/Hammer.DIRECTION_HORIZONTAL
                                                                                     "threshold" 0}))
                                          (js-invoke mc "on" "panstart" #(reset! !start-pan @!pan))
                                          (js-invoke mc "on" "pan" #(let [pan-pos      (int (+ @!start-pan (.-deltaX %)))
                                                                          current-year (nth (visible-period @!caret) (Math.floor (/ (Math.abs pan-pos) width)))]
                                                                      (reset! !pan pan-pos)
                                                                      (when (or (= @!caret (+ current-year 2))
                                                                                (= @!caret (- current-year 2)))
                                                                        (->> (if (> current-year @!caret)
                                                                               (- pan-pos (* width -1))
                                                                               (+ pan-pos (* width -1)))
                                                                             (reset! !start-pan)
                                                                             (reset! !pan))
                                                                        (reset! !caret current-year))))
                                          (reset! !hammer-manager mc)))
                 :component-will-mount (fn [_]
                                         (when-let [mc @!hammer-manager]
                                           (js-invoke mc "destroy")))
                 :reagent-render (fn []
                                   (let [t-year    (:year @timeline)
                                         t-quarter (:quarter @timeline)
                                         t-months  (:months @timeline)]
                                     (on-change @period)
                                     [:div.Timeline-wrapper
                                      [:div.Timeline {:style (transform @!pan)}
                                       [container {:no-wrap true}
                                        (for [year (visible-period @!caret)]
                                          ^{:key (str "timeline-" year)}
                                          [container {:direction "column" :fill true :no-gap true}
                                           (when (not show-years?)
                                             ^{:key "year"}
                                             [container {:justify "space-around" :fill true :no-gap true}
                                              [:span year]])
                                           (when show-months?
                                             ^{:key "months"}
                                             [container {:justify "space-around" :no-wrap true :no-gap true}
                                              (for [month (take 12 months)]
                                                ^{:key (str year "-" month)}
                                                [button {:on-click #(click-month year [month])
                                                         :flat?     (or (not (nil? t-quarter))
                                                                       (not= year t-year)
                                                                       (not= (count t-months) 1)
                                                                       (not (some #(= % month) t-months)))
                                                         :class    [:fill]} (subs month 0 3)])])
                                           (when show-twomonths?
                                             ^{:key "two-months"}
                                             [container {:justify "space-around" :fill true :no-gap true}
                                              (for [month (partition 2 (take 12 months))]
                                                ^{:key (str year "-" (first month) "-" (last month))}
                                                [button {:on-click #(click-month year month)
                                                         :flat?     (or (not (nil? t-quarter))
                                                                       (not= year t-year)
                                                                       (not= (count t-months) 2)
                                                                       (not (some #(= % (first month)) t-months))
                                                                       (not (some #(= % (last month)) t-months)))
                                                         :class    [:fill]} (str (subs (first month) 0 3) "-"
                                                                                 (subs (last month) 0 3))])])
                                           (when show-quarters?
                                             ^{:key "quarters"}
                                             [container {:justify "space-around" :fill true :no-gap true}
                                              (for [quarter (take 4 quarters)]
                                                ^{:key (str year "-" quarter)}
                                                [button {:on-click #(click-quarter year quarter)
                                                         :flat?     (or (empty? quarter)
                                                                       (not= quarter t-quarter)
                                                                       (not= year t-year))
                                                         :class    [:fill]} quarter])])
                                           (when show-tertial?
                                             ^{:key "tertial"}
                                             [container {:justify "space-around" :fill true :no-gap true}
                                              (for [tertial (partition 4 (take 12 months))]
                                                ^{:key (str year "-" (first tertial) "-" (last tertial))}
                                                [button {:on-click #(click-month year tertial)
                                                         :flat?     (or (not (nil? t-quarter))
                                                                       (not= year t-year)
                                                                       (not= (count t-months) 4)
                                                                       (not (some #(= % (first tertial)) t-months))
                                                                       (not (some #(= % (last tertial)) t-months)))
                                                         :class    [:fill]} (str (first tertial) " - " (last tertial))])])
                                           (when show-years?
                                             ^{:key "years"}
                                             [container {:fill true :no-gap true :justify "center"}
                                              ^{:key (str year)}
                                              [button {:on-click #(click-year year)
                                                       :flat?     (or (not (nil? t-months))
                                                                     (not (nil? t-quarter))
                                                                     (not= year t-year))
                                                       :class    [:fill]} year]])])]]]))})))))

(spec/fdef timeline
        :args ::timeline-args
        :ret vector?)
