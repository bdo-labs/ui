(ns ui.element.paginator.styles
  (:require [garden.units :as unit]
            [garden.color :as color]
            [clojure.string :as str]))



(defn style [{:keys [primary]}]
  [[:.Paginator {:text-align :center}
    ;; set span to block so that it takes place in the rendering algorithms
    [:span {:display :block}]
    [:ul {:list-style :none
          :margin 0
          :padding 0
          :display :inline-flex
          :flex-direction :row}
     [:li {:padding 0 :margin 0}
      [:span {:padding {:top (unit/em 0.5) :left (unit/em 1) :right (unit/em 1) :bottom (unit/em 0.5)}}]
      [:&.Prev [:span {:padding {:top (unit/em 0.5) :left (unit/em 1) :right (unit/em 1) :bottom (unit/em 0.5)}
                       :background-color primary
                       :color :white}]]
      [:&.Next [:span {:padding {:top (unit/em 0.5) :left (unit/em 1) :right (unit/em 1) :bottom (unit/em 0.5)}
                       :background-color primary
                       :color :white}]]
      [:.active {:background-color (color/lighten primary 10)
                 :color :white}]
      [:span:hover {:background-color (color/lighten primary 10)
                    :color :white
                    :cursor :pointer}]]]]])
