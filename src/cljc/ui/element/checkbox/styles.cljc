(ns ui.element.checkbox.styles
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [garden.units :as unit]
            [garden.color :as color]))

(defcssfn linear-gradient)
(defcssfn scale)
(defcssfn translateX)
(defcssfn translateY)

(defn style
  [{:keys [primary primary]}]
  [[:.Shape {:display        :inline-flex
             :position       :relative
             :flex-direction :column}]
   [#{:.Checkbox :.toggle}
    [:input {:-webkit-appearance :none
             :background         :white
             :border             [[:solid (unit/px 1) :silver]]
             :outline            :none
             :transition [[:200ms :ease]]}
     [:&:active {:transform (scale 0.8)}]]]
   [:.Radio
    [:.Shape {:border-radius (unit/percent 50)}]]
   [:.toggle
    [:&:hover
     [:input {:border-color primary}]]
    [:.Shape {:width        (unit/rem 4)
              :margin-right (unit/rem 1)}]
    [:input {:border-radius (unit/rem 1.2)
             :background    (linear-gradient (unit/deg 45) (color/rgba [0 0 1 0.2]) (color/rgba [0 0 1 0.1]))
             :position      :absolute
             :transition    [[:200ms :ease]]
             :top           0
             :margin        0
             :height        (unit/percent 100)
             :width         (unit/percent 100)
             :z-index       1}]
    [:i {:background      :white
         :box-shadow      [[0 (unit/px 1) (unit/px 2) (color/rgba [0 0 1 0.5])]]
         :display         :flex
         :margin          (unit/rem 0.1)
         :align-items     :center
         :justify-content :center
         :padding         (unit/rem 0.5)
         :align-self      :flex-start
         :position        :relative
         :transition      [[:200ms :ease]]
         :line-height     0
         :height          (unit/rem 1)
         :width           (unit/rem 1)
         :z-index         2
         :border-radius   (unit/percent 50)}]
    [:&.checked
     [:i {:transform (translateX (unit/rem 1.75))}]
     [:input {:background   (linear-gradient (unit/deg 45) (color/lighten primary 5) (color/darken primary 5))
              :border-color (color/darken primary 10)}]]]
   [:.Checkbox {:display     :flex
                :align-items :baseline
                :user-select :none
                :position    :relative}
    [:&:hover
     [:input {:border-color primary}]]
    [:.Shape {:margin-right (unit/rem 0.5)}]
    [:input {:position :relative}]
    [:i {:position         :absolute
         :left             (unit/percent 50)
         :font-size        (unit/rem 2.5)
         :transform-origin [[:center :center]]
         :transform        [[(scale 0) (translateX (unit/percent -50)) (translateY (unit/percent -25))]]
         :transition       [[:100ms :ease]]
         :-webkit-backface-visibility :hidden
         :z-index          2}]
    [:&.indeterminate
     [:i {:transform-origin [[:left :center]]
          :transform        [[(scale 0.75) (translateX (unit/percent -50))]]}]
     [:input {:background   primary
              :border-color (color/darken primary 10)}]]
    [:&.checked
     [:i {:transform [[(scale 1) (translateX (unit/percent -50)) (translateY (unit/percent -25))]]}]
     [:input {:background   primary
              :border-color (color/darken primary 10)}]]
    [:input {:-webkit-appearance :none
             :background         :white
             :border             [[:solid (unit/px 1) :silver]]
             :position           :relative
             :outline            :none
             :transition         [[:200ms :ease]]
             :width              (unit/rem 1.5)
             :height             (unit/rem 1.5)
             :border-radius      (unit/rem 0.2)
             :z-index            1}]]])
