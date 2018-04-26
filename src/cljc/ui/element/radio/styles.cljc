(ns ui.element.radio.styles
  (:require [garden.units :as unit]
            [garden.color :as color]
            [clojure.string :as str]))


(defn style [_]
  [[:.Radiobuttons
    [:ul {:list-style :none
          :margin 0
          :padding 0}]
    [:&.horizontal
     [:ul {:display :inline-flex
           :flex-direction :row}
      [:li {:margin-right (unit/em 1)}
       [:label {:margin-left (unit/px 5)}]]]]
    [:&.vertical
     [:ul {:display :inline-flex
           :flex-direction :column}
      [:li {:margin-bottom (unit/px 2)}
       [:label {:margin-left (unit/px 5)}]]]]]])
