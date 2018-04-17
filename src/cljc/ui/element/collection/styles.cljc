(ns ui.element.collection.styles
  (:require [garden.stylesheet :as stylesheet :refer [calc at-media]]
            [garden.units :as unit]
            [garden.color :as color]))

(defn style [theme]
  [[:.Collection {:background         :white
                  :width              (unit/percent 100)
                  :overflow-scrolling :touch
                  :overflow           :auto
                  :list-style         :none
                  :padding            0
                  :z-index            2}
    ;; TODO Make it an immediate child selector
    ;; Depends on the release of #garden[2]
    [:li {:border-bottom [[:solid (unit/rem 0.1) (color/rgba [150 150 150 0.1])]]
          :padding       [[(unit/rem 1) (unit/rem 2)]]}
     [:&:first-child
      [:.item-area {:display :inline-block
                    :width   (calc (- (unit/percent 100) (unit/px 64)))}]
      [:.collapse-area {:display    :inline-block
                        :text-align :right
                        :width      (unit/px 64)}
       [:.Button {:padding    0
                  :background :transparent
                  :border     :none
                  :min-width  0
                  :width      (unit/px 35)}]
       [:.Icon {:margin 0}]]]
     [:&.readonly {:cursor :not-allowed}]
     [:&.selected {;; :background (color/rgba [0 0 0 0.04])
                   :font-weight :bold
                   :color :black
                   :cursor     :pointer}]
     [:&.intended {:background-color (color/rgba [0 0 0 0.06])
                   :cursor           :pointer}]
     [:&:last-child {:border-bottom :none}]]]])
