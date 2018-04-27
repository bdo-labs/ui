(ns ui.element.breadcrumbs.styles
  (:require [garden.units :as unit]
            [garden.color :as color]
            [clojure.string :as str]))



(defn style [{:keys [primary]}]

  [[:.Breadcrumbs
    [:ul {:list-style :none
          :margin 0
          :padding 0
          :display :inline-flex
          :flex-direction :row}
     [:li
      [:&.separator {:margin {:top 0 :bottom 0 :left (unit/em 0.2) :right (unit/em 0.2)}}]
      [:span {:display :block}]
      [:a {:background-color primary
           :color :white
           :display :block
           :padding {:top (unit/em 0.1) :bottom (unit/em 0.1) :left (unit/em 0.3) :right (unit/em 0.3)}}
       [:&:hover {:background-color (color/lighten primary 10)}]]]]]])
