(ns ui.element.boundary
  (:require #?(:cljs [reagent.core :refer [atom]])
            [garden.units :as unit]
            [ui.util :as u]))


(defn style [{:keys [primary]}]
  [:.Boundary {:position :relative}
   [:.Made-visible {:position :absolute
                    :border [[:dashed (unit/px 1) primary]]
                    :height (unit/percent 100)
                    :width (unit/percent 100)}]])


(defn boundary
  [{:keys [on-mouse-within
           on-mouse-up
           visible?
           offset]
    :as   params} content]
  (let [!element       (clojure.core/atom nil)
        !within?       (atom false)
        content-params (merge (second content)
                              {:ref #(reset! !element %)})
        mouse-up       #(on-mouse-up %)
        mouse-enter    #(reset! !within? true)
        mouse-leave    #(reset! !within? false)
        mouse-move     #(let [dim (.getBoundingClientRect @!element)
                              mouse-x (- (.-pageX %) (.-left dim))
                              mouse-x-pct (* (/ mouse-x (.-width dim)) 100)]
                          (set! (.-mouseX %) mouse-x)
                          (set! (.-mousePercentX %) mouse-x-pct)
                          (on-mouse-within %))]
    [:div.Boundary {:on-mouse-enter mouse-enter
                    :on-mouse-up    mouse-up
                    :on-mouse-move  mouse-move
                    :on-mouse-leave mouse-leave}
     (when visible? [:div.Made-visible])
     (assoc-in content [1] content-params)]))
