(ns ui.virtual.boundary
  (:require [garden.units :as unit]
            [ui.util :as util]
            [clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]))


(defn style []
  [])


(spec/def ::stub
  (spec/with-gen fn?
    (gen/return (constantly nil))))


(spec/def ::id (spec/and string? not-empty))
(spec/def ::in-viewport ::stub)
(spec/def ::on-mouse-enter ::stub)
(spec/def ::on-mouse-leave ::stub)
(spec/def ::on-mouse-inside ::stub)
(spec/def ::on-click-inside ::stub)
(spec/def ::on-mouse-outside ::stub)
(spec/def ::on-click-outside ::stub)
(spec/def ::offset (spec/coll-of int? :min-count 1 :max-count 4))
(spec/def ::visible boolean?)


(defn offset [v]
  (take 4 (cycle v)))


#_(spec/fdef :args (spec/cat :offset ::offset)
           :ret (spec/coll-of int? :count 4)
           :fn #(take 4 (cycle (-> % :args :offset))))


(spec/def ::params
  (spec/keys :opt-un [::id
                      ::in-viewport
                      ::on-mouse-enter
                      ::on-mouse-leave
                      ::on-mouse-inside
                      ::on-click-inside
                      ::on-mouse-outside
                      ::on-click-outside
                      ::offset
                      ::visible]))


(spec/def ::content (spec/or :vec vector?
                             :seq seq?))


(spec/def ::args (spec/cat :params ::params
                           :content ::content))


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


(re-frame/reg-sub ::boundaries util/extract)


(defn js->cljs [obj]
   (js->clj obj :keywordize-keys true))


(defn init-boundaries
  "Keeps hold of all boundaries, so we can spawn fewer DOM-events"
  []
  (let [boundaries @(re-frame/subscribe [::boundaries])]
   (.addEventListener js/document)))


(defn boundary
  "Create a virtual boundary around an element for more fine-grained events"
  [& args]
  (let [{:keys [params content]}   (util/conform-or-fail ::args args)
        {:keys [id
                in-viewport
                on-mouse-enter
                on-mouse-leave
                on-mouse-inside
                on-click-inside
                on-mouse-outside
                on-click-outside
                offset
                visible]
         :or   {offset [0 0 0 0]}} params
        !element                   (clojure.core/atom nil)
        content-params             (merge (second content)
                                          {:ref #(reset! !element %)})]
    (reagent/create-class
     {:display-name           "boundary"
      :component-did-mount    #(re-frame/dispatch [::register id])
      :component-did-update   #(let [coord (js->cljs (.getBoundingClientRect @!element))]
                                 (re-frame/dispatch [::update id coord]))
      :component-will-unmount #(re-frame/dispatch [::un-register id])
      :reagent-render         #(assoc-in content [1] content-params)})))


#_(defn boundary
  [{:keys [on-mouse-within
           on-mouse-up
           on-mouse-leave
           on-mouse-enter
           visible?
           offset]
    :as   params} content]
  (let [!element       (clojure.core/atom nil)
        !within?       (atom true)
        content-params (merge (second content)
                              {:ref #(reset! !element %)})
        mouse-up       #(on-mouse-up %)
        mouse-enter    #(do (reset! !within? true)
                            (when (fn? on-mouse-enter) (on-mouse-leave %)))
        mouse-leave    #(do (reset! !within? false)
                            (when (fn? on-mouse-leave) (on-mouse-leave %)))
        mouse-move     #(when @!within?
                          (let [dim         (.getBoundingClientRect @!element)
                                mouse-x     (- (.-pageX %) (.-left dim))
                                mouse-x-pct (* (/ mouse-x (.-width dim)) 100)]
                            (set! (.-mouseX %) mouse-x)
                            (set! (.-mousePercentX %) mouse-x-pct)
                            (on-mouse-within %)))]
    [:div.Boundary
     (when visible? [:div.Made-visible])
     [(assoc-in content [1] content-params)]
     [:div.Events {:on-mouse-enter mouse-enter
                   :on-mouse-up    mouse-up
                   :on-mouse-move  mouse-move
                   :on-mouse-leave mouse-leave}]]))
