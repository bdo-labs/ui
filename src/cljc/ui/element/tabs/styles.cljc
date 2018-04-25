(ns ui.element.tabs.styles
  (:require [garden.units :as unit]
            [garden.color :as color]
            [clojure.string :as str]))


(defn style [{:keys [element-background primary]}]
  [[:.Tabs {:display :inline-flex
            :flex-direction :column}
    [:ul {:list-style :none
          :margin 0
          :padding 0}
     [:li {:margin 0
           :padding {:left (unit/em 0.8) :right (unit/em 0.8) :top (unit/em 0.5) :bottom (unit/em 0.5)}}
      [:&:hover {:cursor :pointer
                 :background-color element-background}]]]
    ;; horizontal styling
    [:&.horizontal
     [:ul {:display :inline-flex
           :flex-direction :row
           :border-bottom {:color element-background
                           :style :solid
                           :width (unit/px 1)}}
      [:li {:margin {:top 0 :left 0 :right (unit/px 2) :bottom (unit/px -1)}}]
      [:li.active {:background-color :transparent
                   :border-radius {:top-left (unit/px 3)
                                   :top-right (unit/px 3)}
                   :border {:style :solid
                            :color element-background
                            :width (unit/px 1)}
                   :border-bottom-color :white}]]
     [:.Sheet {:margin-top (unit/em 0.5)}]]
    ;; horizontal bars styling
    [:&.horizontal-bars
     [:ul {:display :inline-flex
           :flex-direction :row
           :border {:color element-background
                    :style :solid
                    :width (unit/px 1)
                    :radius (unit/px 5)}}

      [:li {:border-right {:color element-background
                           :style :solid
                           :width (unit/px 1)}}
       [:&:last-child {:border-right-width (unit/px 0)}]]
      [:li.active {:background-color element-background
                   :box-shadow "inset 0 3px 5px rgba(0,0,0,0.125)"}]]
     [:.Sheet {:margin-top (unit/em 0.5)}]]
    ;; vertical styling
    [:&.vertical {:flex-direction :row}
     [:ul {:display :inline-flex
           :flex-direction :column
           :border-right {:color element-background
                           :style :solid
                          :width (unit/px 1)}}
      [:li {:margin {:top 0 :left 0 :right (unit/px -1) :bottom (unit/px 2)}}]
      [:li.active {:background-color :transparent
                   :border-radius {:bottom-left (unit/px 3)
                                   :top-left (unit/px 3)}
                   :border {:style :solid
                            :color element-background
                            :width (unit/px 1)}
                   :border-right-color :white}]]
     [:.Sheet {:margin-left (unit/em 0.5)}]]
    ;; vertical bars styling
    [:&.vertical-bars {:flex-direction :row}
     [:ul {:display :inline-flex
           :flex-direction :column
           :border {:color element-background
                    :style :solid
                    :width (unit/px 1)
                    :radius (unit/px 5)}}
      [:li {:margin {:top 0 :left 0 :right (unit/px -1) :bottom (unit/px 2)}
            :border-bottom {:color element-background
                            :style :solid
                            :width (unit/px 1)}}
       [:&:last-child {:border-bottom-width (unit/px 0)}]]
      [:li.active {:background-color element-background
                   :box-shadow "inset 0 3px 5px rgba(0,0,0,0.125)"}]]
     [:.Sheet {:margin-left (unit/em 0.5)}]]
    ]])
