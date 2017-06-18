(ns ui.element.checkbox
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require [ui.util :as u]
            [clojure.spec :as spec]
            #?(:clj [garden.def :refer [defcssfn]])
            [garden.units :as unit]
            [garden.color :as color]))


(defcssfn linear-gradient)
(defcssfn scale)
(defcssfn translateX)


(defn style
  [{:keys [primary positive]}]
  [[:.Shape {:display        :inline-flex
             :position       :relative
             :flex-direction :column}]
   [#{:.Checkbox :.Toggle}
    [:input {:-webkit-appearance :none
             :background         :white
             :border             [[:solid (unit/px 1) :silver]]
             :outline            :none}]]
   [:.Radio
    [:.Shape {:border-radius (unit/percent 50)}]]
   [:.Toggle
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
    [:&.Checked
     [:i {:transform (translateX (unit/rem 1.75))}]
     [:input {:background   (linear-gradient (unit/deg 45) (color/lighten positive 5) (color/darken positive 5))
              :border-color (color/darken positive 10)}]]]
   [:.Checkbox {:display     :flex
                :user-select :none
                :position    :relative}
    [:.Shape {:margin-right (unit/rem 0.5)}]
    [:input {:position :relative}]
    [:i {:position         :absolute
         :left             (unit/percent 50)
         :top              (unit/rem -0.3)
         :font-size        (unit/rem 2.5)
         :transform-origin [[:center :center]]
         :transform        [[(scale 0) (translateX (unit/percent -50))]]
         :transition       [[:100ms :ease]]
         :z-index          2}]
    [:&.Checked
     [:i {:transform [[(scale 1) (translateX (unit/percent -50))]]}]
     [:input {:background   primary
              :border-color (color/darken primary 10)}]]
    [:input {:-webkit-appearance :none
             :background         :white
             :border             [[:solid (unit/px 1) :silver]]
             :position           :relative
             :outline            :none
             :transition         [[:100ms :ease]]
             :width              (unit/rem 1.5)
             :height             (unit/rem 1.5)
             :border-radius      (unit/rem 0.2)
             :z-index            1}]]])


(spec/def ::id (spec/or :numeric int?
                        :textual (spec/and string? not-empty)))
(spec/def ::checked? boolean?)
(spec/def ::label string?)


(spec/def ::checkbox-params
  (spec/keys
   :req-un [::checked?]
   :opt-un [::id]))


(spec/def ::checkbox-args
  (spec/cat :params ::checkbox-params
            :label ::label))


(defn checkbox
  [& args]
  (let [{:keys [params label]
         :or   {label ""}}        (u/conform-or-fail ::checkbox-args args)
        {:keys [checked? on-click id]
         :or   {id (u/gen-id)}} params]
    [:label {:for   id
             :class (u/names->str [(when-not (some #(= :Toggle %) (:class params)) :Checkbox)
                                   (when checked? :Checked)
                                   (:class params)])}
     [:div.Shape
      [:i (when-not (some #(= :Toggle %) (:class params)) {:class :ion-ios-checkmark-empty})]
      [:input (merge (dissoc params :checked? :id :class)
                     {:id      id
                      :type    :checkbox
                      :checked checked?})]] label]))


(spec/fdef checkbox
        :args ::checkbox-args
        :ret vector?)
