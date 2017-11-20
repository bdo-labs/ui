(ns ui.element.progress-bar
  #?(:cljs (:require-macros [garden.def :refer [defcssfn defkeyframes defstyles]]))
  (:require #?(:clj [garden.def :refer [defcssfn defkeyframes defstyles]])
            [garden.units :as unit]))


(defcssfn translateZ)


(defn style [{:keys [primary]}]
  [[:.Progress-bar {:position :fixed
                    :top      0
                    :left     0
                    :width    (unit/percent 100)
                    :height   (unit/rem 0.25)
                    :z-index  200}
    [:.Progress {:background primary
                 :height     (unit/percent 100)
                 :transition [[:200ms :ease]]
                 :transform  (translateZ 0)
                 :width      0}]]])


(defn progress-bar
  [{:keys [progress]}]
  [:div.Progress-bar
   [:div.Progress {:style {:width (str progress "%")}}]])
