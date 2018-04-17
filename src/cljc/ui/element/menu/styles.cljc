(ns ui.element.menu.styles
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [garden.color :as color]))

(defcssfn cubic-bezier)
(defcssfn translateY)
(defcssfn scale)

;; TODO These !important rules should be avoided
(defn style [theme]
  [[:.Dropdown {:position         :absolute
                :background       :white
                :margin-top       0
                :transform        (scale 1)
                ;; Transition has been temporarily removed due to performance-issues
                :transition       [[:200ms (cubic-bezier 0.770, 0.000, 0.175, 1.000)]]
                :z-index          90
                :font-weight      :normal}
    [:&.not-open {:transform (scale 0)}]
    [:&.origin-top-left {:transform-origin [[:top :left]]}]
    [:&.origin-top-right {:transform-origin [[:top :right]]}]
    [:&.origin-top-center {:transform-origin [[:top :center]]}]
    [:&.origin-bottom-left {:transform-origin [[:bottom :left]]}]
    [:&.origin-bottom-right {:transform-origin [[:bottom :right]]}]
    [:&.origin-bottom-center {:transform-origin [[:bottom :center]]}]]])
