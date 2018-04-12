(ns ui.element.button.styles
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [garden.units :as unit]
            [garden.color :as color]
            [clojure.string :as str]
            [ui.util :as util]))

(defcssfn translateX)
(defcssfn translateY)
(defcssfn translateZ)
(defcssfn scale)
(defcssfn scaleX)

(defn colored [[k color]]
  [(keyword (str "&." (name k)))
   {:background-color color
    :border-color     color
    :color            (if (util/dark? color) :white :black)}
   [:&:hover {:background-color (color/lighten color 10)}]])

(defn style [theme]
  (let [colors (select-keys theme [:primary :secondary :tertiary :positive :negative])]
    [[:.Button {:appearance  :none
                :background  (util/gray 245)
                :border      [[:solid (unit/em 0.1) (util/gray 245)]]
                :color       :inherit
                :cursor      :pointer
                :outline     :none
                :user-select :none
                :min-height   (unit/rem 3)
                :min-width   (unit/rem 8)
                :padding     [[(unit/rem 0.25) (unit/rem 1)]]
                :overflow    :hidden
                :transition  [[:background-color :200ms :ease]]
                :line-height 2.5}
      [:&:hover {:background-color (util/gray 250)}]
      [:&.disabled {:opacity 0.3
                    :pointer-events :none
                    :cursor  :not-allowed}]
      [:.Icon {:font-size      (unit/rem 2)
               :color          :inherit
               :vertical-align :middle
               :min-width      (unit/rem 1)
               :line-height    0}];; Default Colors
      (map colored colors);; Variations
      [:&.circular {:border-radius (unit/percent 50)
                    :padding       (unit/rem 0.25)
                    :min-width     (unit/rem 3.5)}]
      [:&.flat {:background-color :transparent}
       (->> colors (map (fn [[k v]] [(keyword (str "&." (name k))) {:color (color/darken v 10)}])))]
      [:&.pill {:border-radius (unit/em 2)}]
      [:&.raised {:box-shadow [[0 (unit/rem 0.2) (unit/rem 0.3) (color/rgba [0 0 0 0.05])]]
                  :transition [[:50ms :ease-in-out]]
                  :transform  (translateY (unit/em -0.15))}
       [:&:active {:box-shadow [[0 (unit/rem 0.05) (unit/rem 0.08) (color/rgba [0 0 0 0.1])]]
                   :transform  (translateY 0)}]]
      [:&.tab {:background    :transparent
               :border        :none
               :border-bottom [[:solid (unit/px 2) :transparent]]
               :border-radius 0}
       [:&.active {:border-color (:primary colors)}]]
      [:&.nav {:background  :transparent
               :color       (:tertiary colors)
               :line-height 1.5
               :min-height  (unit/rem 1)
               :transform   (translateZ 0)}
       [:&.active {:color (:primary colors)}]
       [:&:before {:background       (:primary colors)
                   :content          "' '"
                   :display          :inline-block
                   :margin           (unit/rem 0.5)
                   :width            (unit/rem 0)
                   :height           (unit/px 1)
                   :line-height      0
                   :transform-origin [[:left :center]]
                   :transition       [[:200ms :ease]]}]
       [:&.active:before {:width (unit/rem 2)}]]]]))

