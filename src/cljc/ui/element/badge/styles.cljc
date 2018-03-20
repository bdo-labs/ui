(ns ui.element.badge.styles
  (:require [garden.units :as unit]
            [garden.color :as color]))

(defn style [{:keys [primary secondary]}]
  [[:.Badge {:background    (color/lighten primary 15)
             :border-radius (unit/em 1)
             :color         (color/darken primary 15)
             :display       :block
             :padding       (unit/px 2)
             :min-height    (unit/px 3)
             :min-width     (unit/px 3)
             :font-size     (unit/px 10)
             :position      :absolute
             :z-index       99
             :animation     [[:scaled :200ms :ease]]}
    [:&.show-count {:min-width   (unit/px 6)
                    :height      (unit/px 6)
                    :line-height (unit/px 7)
                    :padding     (unit/em 0.5)}]]])
