(ns ui.virtual.boundary
  (:require [garden.units :as unit]
            [goog.events :as events]
            [goog.events.EventType]
            [ui.util :as util]
            [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as spec]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]))


;; Views ------------------------------------------------------------------


(defn boundary
  "Create a virtual boundary around an element for more fine-grained events"
  [& args]
  (let [{:keys [params content]} (util/conform-or-fail ::args args)
        {:keys [id
                lift
                offset]
         :or   {offset [0]}}     params
        offset                   (take 4 (cycle offset))
        [top left bottom right]  offset
        element*                 (clojure.core/atom nil)
        content-params           (merge (second content)
                                        {:ref #(reset! element* %)})]
    (reagent/create-class
     {:display-name           "boundary"
      :component-did-mount    #(let [coord     (util/js->cljs (.getBoundingClientRect @element*))
                                     adj-coord (-> coord
                                                   (update-in [:top] - top)
                                                   (update-in [:y] - top)
                                                   (update-in [:height] + (+ top bottom))
                                                   (update-in [:bottom] + bottom)
                                                   (update-in [:left] - left)
                                                   (update-in [:x] - left)
                                                   (update-in [:width] + (+ left right))
                                                   (update-in [:right] + right))]
                                 (re-frame/dispatch [::register id])
                                 (re-frame/dispatch [::update id adj-coord])
                                 (when (true? lift)
                                   (set! (.-position (.-style @element*)) "absolute")
                                   (set! (.-left (.-style @element*)) (str (:left coord) "px"))
                                   (set! (.-top (.-style @element*)) (str (:top coord) "px"))
                                   ;; TODO Should we swap with this dummy upon changes to the viewport?
                                   (when-let [dummy (.insertBefore @element* (.createElement js/document "div") nil)]
                                     (set! (.-width (:width coord)))
                                     (set! (.-height (:height coord)))
                                     (.appendChild (.-body js/document) @element*))))
      :component-did-update   #(let [coord (util/js->cljs (.getBoundingClientRect @element*))]
                                 (re-frame/dispatch [::update id coord]))
      :component-will-unmount #(re-frame/dispatch [::un-register id])
      :reagent-render         #(assoc-in content [1] content-params)})))



;; Specifications ---------------------------------------------------------


(spec/def ::id (spec/and string? not-empty))
(spec/def ::offset (spec/coll-of int? :min-count 1 :max-count 4))
(spec/def ::lift boolean?)
(spec/def ::content vector?)


(spec/def ::params
  (spec/keys :req-un [::id]
             :opt-un [::offset ::lift]))


(spec/def ::args (spec/cat :params ::params :content ::content))


;; Events -----------------------------------------------------------------


(re-frame/reg-event-db
 ::set-mouse-position
 (fn [db [_ pos]]
   (assoc db ::mouse-position pos)))


(re-frame/reg-event-db
 ::set-mouse-click
 (fn [db [_ click]]
   (assoc db ::mouse-click click)))


(re-frame/reg-event-db
 ::set-viewport
 (fn [db [_ coord]]
   (assoc db ::viewport coord)))


(re-frame/reg-event-db
 ::register
 (fn [db [_ id]] (assoc-in db [::boundaries id] {})))


(re-frame/reg-event-db
 ::update
 (fn [db [_ id m]] (assoc-in db [::boundaries id] m)))


(re-frame/reg-event-db
 ::un-register
 (fn [db [_ id]]
   (update-in db [::boundaries] dissoc id)))


;; Subscriptions ----------------------------------------------------------


(re-frame/reg-sub ::boundaries util/extract)
(re-frame/reg-sub ::mouse-position util/extract)
(re-frame/reg-sub ::mouse-click util/extract)
(re-frame/reg-sub ::viewport util/extract)


(re-frame/reg-sub
 ::mouse-inside
 :<- [::boundaries]
 :<- [::mouse-position]
 (fn [[boundaries position] [_ id]]
   (when (some-> (:x position)
                 (:x boundaries))
     (when-let [boundary (get boundaries id)]
       (and (> (:x position) (:x boundary))
            (< (:x position) (+ (:x boundary) (:width boundary)))
            (> (:y position) (:y boundary))
            (< (:y position) (+ (:y boundary) (:height boundary))))))))


(re-frame/reg-sub
 ::click-outside
 (fn [[_ id]]
   [(re-frame/subscribe [::mouse-click])
    (re-frame/subscribe [::mouse-inside id])])
 (fn [[click inside] _]
   (and click (not inside))))


(re-frame/reg-sub
 ::in-viewport
 :<- [::boundaries]
 :<- [::viewport]
 (fn [[boundaries viewport] [_ id]]
   (when-let [boundary (get boundaries id)]
     (util/log boundary viewport)
     (and (<= (:top boundary) (:height viewport))
          (<= (:left boundary) (:width viewport))))))


;; Events -----------------------------------------------------------------


(def event
  {:mousemove   goog.events.EventType.MOUSEMOVE
   :mousedown   goog.events.EventType.MOUSEDOWN
   :mouseup     goog.events.EventType.MOUSEUP
   :load        goog.events.EventType.LOAD
   :scroll      goog.events.EventType.SCROLL
   :resize      goog.events.EventType.RESIZE
   :orientation goog.events.EventType.ORIENTATIONCHANGE})


(defn listen [ks f]
  (->> [ks]
       (flatten)
       (mapv #(events/listen js/window (-> event %) f))))


(defn on-mouse-up []
  (re-frame/dispatch [::set-mouse-click false]))


(defn on-mouse-down []
  (re-frame/dispatch [::set-mouse-click true]))


(defn on-mouse-move [e]
  (let [pos {:x (.-screenX e)
             :y (.-screenY e)}]
    (re-frame/dispatch [::set-mouse-position pos])))


;; FIXME Needs to be container-aware and take scroll into account
(defn on-viewport-change [e]
  (let [width  (max (or (.-clientWidth (.-documentElement js/document))
                        (.-innerWidth js/window)))
        height (max (or (.-clientHeight (.-documentElement js/document))
                        (.-innerHeight js/window)))
        coord {:width width
               :height height}]
    (re-frame/dispatch [::set-viewport coord]))) 


(listen :mouseup on-mouse-up)
(listen :mousedown on-mouse-down)
(listen :mousemove on-mouse-move)
(listen [:load :resize :orientation] on-viewport-change)
