(ns ui.element.modal.styles
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [garden.units :as unit]
            [garden.color :as color]))

(defcssfn translateX)
(defcssfn translateY)

(defn style [{:keys [primary secondary background]}]
  [[:.Dialog {:position :fixed
              :left     0
              :top      0
              :height   (unit/percent 100)
              :width    (unit/percent 100)
              :margin   [[0 :!important]]
              :z-index  100}
    ;; [(selector/& (selector/not :.Open)) {:display :none}]
    [:&.show
     [:.Backdrop {:opacity   1
                  :animation [[:fade :200ms :ease]]
                  :z-index 99}]]
    [:.Content {:position  :absolute
                :left      (unit/percent 50)
                :top       (unit/percent 50)
                :transform [[(translateY (unit/percent -50)) (translateX (unit/percent -50))]]
                :z-index 101}
     [:.Container
      ;; TODO This hack should be corrected!
      [:&.raised {:overflow :visible}]]]]
   [:.Close {:position :absolute
             :cursor :pointer
             :top 0
             :right (unit/rem 1)
             :z-index 104}]
   [:body {:background-color background}]
   [:menu [:a {:display :block}]]
   [:a {:color           secondary
        :text-decoration :none}
    [:&.primary {:color primary}]
    [:&:hover {:color (color/darken secondary 30)}
     [:&.primary {:color primary}]]]])
