(ns ui.element.chooser.styles
  (:require [garden.units :as unit]))

(defn style [theme]
  [[:.Chooser {:position      :relative
               :width         (unit/percent 100)
               :margin-bottom (unit/rem 1)}
    [:.Badge {:top   (unit/rem 1)
              :right (unit/rem -0.5)
              :position :absolute}]
    [:.Dropdown {:border-top-left-radius  0
                 :border-top-right-radius 0
                 :max-height              (unit/rem 25)
                 :width                   (unit/percent 100)}]]])
