(ns ui.element.clamp.styles
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [garden.units :as unit]
            [garden.color :as color]))


(defcssfn attr)
(defcssfn scale)
(defcssfn translateX)
(defcssfn translateY)
(defcssfn translateZ)


(defn dark?
  "Is the [r g b]-color supplied a dark color?"
  [[r g b]]
  (> (- 1 (/ (+ (* 0.299 r) (* 0.587 g) (* 0.114 b)) 255)) 0.5))

(defn style
  [{:keys [primary secondary]}]
  [[:.Clamp
    {:box-sizing :border-box,
     :padding (unit/rem 1),
     :width (unit/percent 100),
     :text-align :center,
     :transform (translateZ 0),
     :position :relative}
    [:.Label
     {:text-align :left,
      :font-weight :normal,
      :margin-bottom (unit/rem 2),
      :overflow :hidden,
      :white-space :nowrap,
      :text-overflow :ellipsis,
      :font-size (unit/em 1.2)}]
    [:.Slider {:height (unit/px 2), :background :silver}]
    [:.Extract
     {:background primary, :position :absolute, :height (unit/percent 100)}]
    [:.Knob
     {:background :white,
      :border-radius (unit/percent 50),
      :border [[:solid (unit/px 2) primary]],
      :position :absolute,
      :transform-origin [[:center :center]],
      :transition [[:200ms :ease]],
      :transform [[(translateY (unit/percent -45)) (translateZ 0)]],
      :cursor :pointer,
      :height (unit/rem 0.5),
      :width (unit/rem 0.5),
      :z-index 10} [#{:&:hover :&.Dirty} {:background primary}]
     [:&:hover {:transform [[(translateY (unit/percent -50)) (scale 1.4)]]}
      [:&:after {:opacity 1}]]
     [:&:after
      {:display :block,
       :background secondary,
       ;; :color         (if (dark? (vals (select-keys secondary [:red :green
       ;; :blue])))
       ;;                  (color/darken "#fff" 5) (color/lighten "#000" 5))
       :border-radius (unit/em 0.2),
       :padding [[(unit/em 0.25) (unit/em 0.5)]],
       :transition [[:200ms :ease]],
       :opacity 0,
       :left (unit/percent 50),
       :font-size (unit/em 0.7),
       :transform (translateX (unit/percent -50)),
       :position :absolute,
       :bottom (unit/em 1.5),
       :content (attr :data-value)}]]]])

