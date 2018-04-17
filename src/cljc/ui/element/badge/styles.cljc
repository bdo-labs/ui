(ns ui.element.badge.styles
  (:require [garden.units :as unit]
            [garden.color :as color]))

(defn style [{:keys [primary secondary]}]
  [[:.Badge {:background (color/lighten primary 15)
             :color      (color/darken primary 15)
             :display    :inline-block
             :margin     [[(unit/em 0.25) (unit/em 0.5)]]
             :min-height (unit/px 3)
             :min-width  (unit/px 3)
             :font-size  (unit/px 10)
             :z-index    99
             :animation  [[:scaled :200ms :ease]]}
    [:&.number {:padding       (unit/em 0.25)
                :border-radius (unit/em 1)}]
    [:&.string {:padding       [[(unit/em 0.25) (unit/em 0.5)]]
                :border-radius (unit/em 1.5)}]
    [:&.show-content {:min-width   (unit/px 6)
                      :height      (unit/px 6)
                      :line-height (unit/px 7)
                      :padding     (unit/em 0.5)}]]])
