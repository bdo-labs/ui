(ns ui.element.boundary
  (:require #?(:cljs [reagent.core :refer [atom]])
            [garden.units :as unit]
            [ui.util :as util]
            [clojure.test.check.generators :as gen]
            [clojure.spec :as spec]))


(defn style [{:keys [primary]}]
  [[:.Boundary {:position :relative}
    [:.Events {:position :absolute
               :box-sizing :content-box
               :width    (unit/percent 100)
               :padding  [[(unit/rem 3) 0]]
               :height   (unit/percent 100)}]
    [:.Made-visible {:position :absolute
                     :border   [[:dashed (unit/px 1) primary]]
                     :height   (unit/percent 100)
                     :width    (unit/percent 100)}]]])

(spec/def ::stub
  (spec/with-gen fn?
    (gen/return (constantly nil))))
(spec/def ::in-viewport ::stub)
(spec/def ::on-mouse-enter ::stub)
(spec/def ::on-mouse-leave ::stub)
(spec/def ::on-click-outside ::stub)
(spec/def ::offset (spec/coll-of int? :min-count 1 :max-count 4))
(spec/def ::visible boolean?)
(spec/def ::params
  (spec/keys :opt-un [::in-viewport
                      ::on-mouse-enter
                      ::on-mouse-leave
                      ::on-click-outside
                      ::offset
                      ::visible]))
(spec/def ::content (spec/or :vec vector?
                             :seq seq?))
(spec/def ::args (spec/cat :params ::params :content ::content))

(defn offset [v]
  (take 4 (cycle v)))

#_(spec/fdef :args (spec/cat :offset ::offset)
           :ret (spec/coll-of int? :count 4)
           :fn #(take 4 (cycle (-> % :args :offset))))


(defn boundary [& args]
  (let [{:keys [params content]} (util/conform-or-fail ::args args)
        {:keys [in-viewport
                on-mouse-enter
                on-mouse-leave
                on-click-outside
                offset
                visible]
         :or {offset [0 0 0 0]}} params
        !element (clojure.core/atom nil)
        content-params (merge (second content)
                              {:ref #(reset! !element %)})]
    (assoc-in content [1] content-params)))


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
