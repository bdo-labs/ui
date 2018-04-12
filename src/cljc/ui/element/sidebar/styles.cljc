(ns ui.element.sidebar.styles
  #?(:cljs (:require-macros [garden.def :refer [defcssfn defkeyframes]]))
  (:require #?(:clj [garden.def :refer [defcssfn defkeyframes]])
            [garden.stylesheet :as stylesheet :refer [calc at-media]]
            [garden.media :as media]
            [garden.units :as unit]
            [garden.color :as color]))

(defn style [{:keys [background] :as theme}]
  ;; Should use immediate descender-selector
  ;; [[:.Sidebar.locked.to-the-left :.Slider :main] {:margin-left (unit/px 360)}]
  [[:.Sidebar {:overflow :hidden
               :width    (unit/percent 100)
               :height   (unit/percent 100)}
    ;; (at-media (media/query {:max-width "600px"})
    ;;           [:&.Sidebar {:display :none}])
    [:.Slider {:width    (unit/percent 100)
               :height   (unit/percent 100)
               :position :relative}]
    [:sidebar {:width       (calc (- (unit/px 360) (unit/rem 6)))
               :overflow    :auto
               :background  background
               :box-shadow  [[(unit/em 0.1) (unit/em 0.1) (unit/em 0.5) (color/rgba [0 0 0 0.2])]]
               :height      (unit/percent 100)
               :padding     [[0 (unit/rem 3)]]
               :position    :absolute
               :top         0
               :z-index     9
               :line-height 2}]
    [:main {:width              (unit/percent 100)
            :overflow           :auto
            :overflow-scrolling :touch
            :display            :flex
            :position           :relative
            :height             (unit/percent 100)}]
    [:&.locked
     [:main {:width (calc (- (unit/percent 100) (unit/px 360)))}]]
    #_[(selector/& (selector/not :.locked))
       [:.Slider {:transition [[:500ms :ease]]}]
       [:&.to-the-left
        [:sidebar {:left (unit/px -360)}]]
       [:&.to-the-right
        [:sidebar {:right (unit/px -360)}]]
       [:&.ontop
        [:sidebar {:transition [[:500ms :ease]]}]]
       [:&.open
        [:.Backdrop {:opacity 1
                     :z-index 8}]
        [(selector/& (selector/not :.ontop))
         [:&.to-the-left [:.Slider {:transform (translateX (unit/px 360))}]]
         [:&.to-the-right [:.Slider {:transform (translateX (unit/px -360))}]]]
        [:&.ontop
         [:sidebar {:box-shadow [[0 0 (unit/rem 6) (color/rgba [0 0 0 0.5])]]}]
         [:&.to-the-left [:sidebar {:transform (translateX (unit/px 360))}]]
         [:&.to-the-right [:sidebar {:transform (translateX (unit/px -360))}]]]]]]])
