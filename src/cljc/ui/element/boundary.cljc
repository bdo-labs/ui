(ns ui.element.boundary
  (:require #?(:cljs [reagent.core :refer [atom]])
            [garden.units :as unit]
            [ui.util :as u]))


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


(defn boundary
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
     (assoc-in content [1] content-params)
     [:div.Events {:on-mouse-enter mouse-enter
                   :on-mouse-up    mouse-up
                   :on-mouse-move  mouse-move
                   :on-mouse-leave mouse-leave}]]))
