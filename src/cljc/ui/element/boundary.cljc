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
  [{:keys [on-mouse-within visible? offset]
    :as   params} content]
  (let [!element       (clojure.core/atom nil)
        !within?       (atom false)
        content-params (merge (second content)
                              {:ref #(reset! !element %)})
        mouse-enter    #(reset! !within? true)
        mouse-leave    #(reset! !within? false)
        mouse-move     #(when @!within?
                          (let [mouse-x (- (.-pageX %) (.-left (.getBoundingClientRect @!element)))]
                            (set! (.-mouseX %) mouse-x)
                            (on-mouse-within %)))]
    [:div.Boundary {:on-mouse-enter mouse-enter
                    :on-mouse-move  mouse-move
                    :on-mouse-leave mouse-leave}
     (when visible? [:div.Made-visible])
     (assoc-in content [1] content-params)]))
