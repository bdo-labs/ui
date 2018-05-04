(ns ui.element.calendar.styles
  (:require [garden.units :as unit]
            [garden.color :as color]))


(defn style [{:keys [primary secondary tertiary]}]
  [[:.Date-picker {:position :relative
                   :width    (unit/percent 100)}
    [:.Calendar {:background :white
                 :z-index    3}]]
   [:.Calendar
    [:table {:border-collapse :collapse
             :user-select     :none
             :text-align      :center
             :table-layout    :fixed}]
    [:th {:padding (unit/rem 2)}]
    [:td {:padding (unit/rem 1)}]
    [#{:.Previous :.Next} {:color (color/lighten tertiary 40)}]
    [:.Day {:pointer :not-allowed}
     [:&.between {:position :relative}
      [:&:first-child
       [:span:before {:border-bottom-right-radius 0
                       :border-top-right-radius    0
                       :right                      0
                       :left                       (unit/percent 50)}]]
      [:span:before {:background    (color/lighten primary 30)
                      :display       :block
                      :content       "' '"
                      :height        (unit/em 2)
                      :width         (unit/em 1)
                      :border-radius (unit/percent 50)}]]
     [:&.selectable {:cursor :pointer}
      [:&:hover [:span {:background (color/lighten primary 30)}]]]
     [:span {:border-radius (unit/percent 50)
             :line-height   (unit/em 1.2)
             :display       :inline-block
             :padding       (unit/rem 1)
             :height        (unit/rem 2)
             :width         (unit/rem 2)}]
     [:&.today
      [:span {:border [[:solid (unit/px 1) (color/darken primary 10)]]}]]
     [:&.selected.selectable {:color  :white
                              :cursor :default}
      [:span {:background primary}]]]]])
