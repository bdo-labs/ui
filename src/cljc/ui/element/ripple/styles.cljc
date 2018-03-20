(ns ui.element.ripple.styles
  (:require [garden.units :as unit]
            [garden.color :as color]))

(defn style [theme]
  [[:.Ripple {:fill     (color/rgba [0 0 0 0.2])
              :height   (unit/percent 100)
              :width    (unit/percent 100)
              :top      0
              :left     0
              :opacity  0
              :position :absolute}
    [:&.animate {:animation [[:scale-ripple :500ms :ease]]}]]])

