(ns ui.element.textfield.styles
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [garden.stylesheet :refer [calc]]
            [garden.selectors :as s]
            [garden.units :as unit]
            [garden.color :as color]
            [clojure.string :as str]
            [ui.util :as util]))

(defcssfn translateY)
(defcssfn translateZ)
(defcssfn scale)

(defn style [{:keys [primary secondary positive negative]}]
  [[:.Textfield {:position      :relative
                 :border-bottom [[(unit/px 1) :solid :silver]]
                 :margin        0
                 :display       :inline-block
                 :width         (unit/percent 100)}
    [#{:&.focus :&:hover} {:border-color primary}]
    [:&.dirty [:input {:color :black}]]
    [:&.label {:margin-top (unit/rem 3)}]
    [:&.required
     [:label
      [:&:after {:content        "*"
                 :color          :inherit
                 :display        :inline-block
                 :font-size      (unit/em 0.8)
                 :vertical-align :top
                 :margin-left    (unit/em 0.3)}]]
     [:&.dirty
      [:&.valid {:border-color positive}
       [:label {:color positive}
        [:&:after {:content "✓"}]]]
      [:&.invalid {:border-color negative}
       [:label {:color negative}
        [:&:after {:content "✗"}]]]]]
    [#{:&.not-empty :&.placeholder}
     [:label {:left             0
              :transform        [[(translateY (unit/percent -100)) (scale 0.75)]]
              :transform-origin [[:top :left]]}]]
    [:label {:position      :absolute
             :color         :silver
             :transition    [[:all :200ms :ease]]
             :transform     (translateZ 0)
             :left          0
             :cursor        :text
             :overflow      :hidden
             :text-overflow :ellipsis
             :white-space   :nowrap
             :width         (calc (- (unit/percent 100) (unit/em 1.5)))
             :top           (unit/rem 0.5)
             :z-index       1}]
    [:input {:background         :transparent
             :border             :none
             :box-sizing         :border-box
             :color              (color/rgb [90 90 90])
             :display            :inline-block
             :font-weight        :600
             :outline            :none
             :overflow           :hidden
             :padding            [[(unit/rem 0.5) 0]]
             :position           :relative
             :text-overflow      :ellipsis
             :transition         [[:all :200ms :ease]]
             :white-space        :nowrap
             :width              (unit/percent 100)
             :-webkit-appearance :none
             :z-index            2}
     [:&:focus {:width (calc (- (unit/percent 100) (unit/em 3)))}
      [:+ [:label {:color            :black
                   :left             0
                   :transform        [[(translateY (unit/percent -100)) (scale 0.75)]]
                   :transform-origin [[:top :left]]}]]]]
    [:.Ghost {:color    (color/rgba [0 0 0 0.3])
              :position :absolute
              :top      (unit/rem 0.5)}]]])
