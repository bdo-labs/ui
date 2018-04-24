(ns ui.wire.form.styles
  (:require [garden.color :as color]
            [garden.units :as unit]))


(defn style [{:keys [background]}]
  [[:.Wizard {:display :flex
              :flex-direction :column
              :justify-content :space-between
              :padding (unit/em 1)
              :background-color (color/rgb [240 240 240])
              :border-radius (unit/px 4)}

    [:.Legend {:text-align :center}]

    [:.Pagination {:flex-direction :row
                   :display :flex
                   :justify-content :space-between
                   :margin-top (unit/em 1.5)}]]])
