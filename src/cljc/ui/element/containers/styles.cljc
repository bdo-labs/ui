(ns ui.element.containers.styles
  (:require [garden.units :as unit]
            [garden.color :as color]))

(defn style [theme]
  [[:.Flex {:flex 1}]
   #_[".Container:not(.layout-vertically):not(.Compact) > * + *" {:margin-left (unit/rem 1)}]
   #_[".Container.layout-vertically:not(.Compact) > * + *" {:margin-top (unit/rem 1)}]
   [:.Container {:flex-grow   1
                 :flex-shrink 0
                 :flex-basis  :auto
                 :box-sizing  :border-box
                 :display     :flex}
    [:&.hide {:display :none}]
    [:&.gap {:padding (unit/rem 2)}]
    [:&.wrap {:flex-wrap :wrap}]
    ;; [(selector/& (selector/not :.layout-vertically)) {:flex-direction :row}]
    ;; [(selector/& (selector/not :.No-wrap)) {:flex-wrap :wrap}]
    ;; [(selector/& (selector/not :.No-gap)) {:padding (unit/rem 2)}]
    ;; [(selector/& (selector/not (selector/attr-contains :class "Align"))) {:align-items :flex-start}]
    ;; [(selector/& (selector/not (selector/attr-contains :class "Justify"))) {:justify-content :flex-start}]
    [:&.layout-vertically {:flex-direction :column}
     [:&.fill {:height (unit/percent 100)}]]
    [:&.align-start-start {:justify-content :flex-start
                           :align-items     :flex-start}]
    [:&.align-start-end {:justify-content :flex-start
                         :align-items     :flex-end}]
    [:&.align-start-center {:justify-content :flex-start
                            :align-items     :center}]
    [:&.align-end-end {:justify-content :flex-end
                       :align-items     :flex-end}]
    [:&.align-end-start {:justify-content :flex-end
                         :align-items     :flex-start}]
    [:&.align-end-center {:justify-content :flex-end
                          :align-items     :center}]
    [:&.align-center-center {:justify-content :center
                             :align-items     :center}]
    [:&.align-center-start {:justify-content :center
                            :align-items     :flex-start}]
    [:&.align-center-end {:justify-content :center
                          :align-items     :flex-end}]
    [:&.space-between {:justify-content :space-between}]
    [:&.space-around {:justify-content :space-around}]
    [:&.space-none {:align-items :stretch}]
    [:&.scrollable {:min-height :min-content
                    :overflow   :auto
                    :-webkit-overflow-scrolling :touch}]
    ;; TODO https://github.com/noprompt/garden/issueselector/127
    #_[(selector/& :.Container (selector/> (selector/not :.Compact) (selector/+ :* :*))) {:margin-left (unit/rem 2)}]
    [#{:&.fill :.fill} {:box-sizing :border-box
                        :flex       1
                        :min-width  0
                        :min-height 0
                        :width      (unit/percent 100)}]
    [:&.inline {:display :inline-flex}]
    [:&.rounded {:border-radius (unit/rem 1)}]
    [:&.raised {:box-shadow [[0 (unit/rem 0.2) (unit/rem 0.3) (color/rgba [35 35 35 0.2])]]
                :overflow :auto}]]])

