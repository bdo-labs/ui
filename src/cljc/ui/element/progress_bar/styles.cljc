(ns ui.element.progress-bar.styles
  #?(:cljs (:require-macros [garden.def :refer [defcssfn defkeyframes defstyles]]))
  (:require #?(:clj [garden.def :refer [defcssfn defkeyframes defstyles]])
            [garden.color :as color]
            [garden.units :as unit]))


(defcssfn translateZ)


(defn style [{:keys [primary]}]
  [[:.Progress-bar {:position :fixed
                    :left     0
                    :width    (unit/percent 100)
                    :height   (unit/rem 0.25)
                    :z-index  200}
    [:&.align-top {:top 0}]
    [:&.align-header-bottom {:top (unit/px 64)}]
    [:&.align-bottom {:bottom 0}]
    [:.Progress {:background (color/lighten primary 10)
                 :height     (unit/percent 100)
                 :transition [[:200ms :ease]]
                 :transform  (translateZ 0)
                 :width      0}]]])
