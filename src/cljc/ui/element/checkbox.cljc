(ns ui.element.checkbox
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [ui.util :as u]
            [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [garden.units :as unit]
            [garden.color :as color]))


(defcssfn linear-gradient)
(defcssfn scale)
(defcssfn translateX)
(defcssfn translateY)


(defn style
  [{:keys [primary primary]}]
  [[:.Shape {:display        :inline-flex
             :position       :relative
             :flex-direction :column}]
   [#{:.Checkbox :.toggle}
    [:input {:-webkit-appearance :none
             :background         :white
             :border             [[:solid (unit/px 1) :silver]]
             :outline            :none}]]
   [:.Radio
    [:.Shape {:border-radius (unit/percent 50)}]]
   [:.toggle
    [:&:hover
     [:input {:border-color primary}]]
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
    [:&.checked
     [:i {:transform (translateX (unit/rem 1.75))}]
     [:input {:background   (linear-gradient (unit/deg 45) (color/lighten primary 5) (color/darken primary 5))
              :border-color (color/darken primary 10)}]]]
   [:.Checkbox {:display     :flex
                :align-items :baseline
                :user-select :none
                :position    :relative}
    [:&:hover
     [:input {:border-color primary}]]
    [:.Shape {:margin-right (unit/rem 0.5)}]
    [:input {:position :relative}]
    [:i {:position         :absolute
         :left             (unit/percent 50)
         :font-size        (unit/rem 2.5)
         :transform-origin [[:center :center]]
         :transform        [[(scale 0) (translateX (unit/percent -50)) (translateY (unit/percent -25))]]
         :transition       [[:100ms :ease]]
         :-webkit-backface-visibility :hidden
         :z-index          2}]
    [:&.indeterminate
     [:i {:transform-origin [[:left :center]]
          :transform        [[(scale 0.75) (translateX (unit/percent -50))]]}]
     [:input {:background   primary
              :border-color (color/darken primary 10)}]]
    [:&.checked
     [:i {:transform [[(scale 1) (translateX (unit/percent -50)) (translateY (unit/percent -25))]]}]
     [:input {:background   primary
              :border-color (color/darken primary 10)}]]
    [:input {:-webkit-appearance :none
             :background         :white
             :border             [[:solid (unit/px 1) :silver]]
             :position           :relative
             :outline            :none
             :transition         [[:200ms :ease]]
             :width              (unit/rem 1.5)
             :height             (unit/rem 1.5)
             :border-radius      (unit/rem 0.2)
             :z-index            1}]]])

(spec/def ::maybe-fn
  (spec/with-gen fn?
    (gen/return (constantly nil))))

(spec/def ::id (spec/or :numeric int?
                        :textual (spec/and string? not-empty)))
(spec/def ::checked? boolean?)
(spec/def ::label string?)
(spec/def ::value string?)

(spec/def ::on-change ::maybe-fn)


(spec/def ::checkbox-params
  (spec/keys
   :opt-un [::id
            ::checked?
            ::value
            ::on-change]))


(spec/def ::checkbox-args
  (spec/cat :params ::checkbox-params
            :label ::label))


(defn checkbox
  [& args]
  (let [{:keys [params label]
         :or   {label ""}}      (u/conform! ::checkbox-args args)
        {:keys [checked on-click id]
         :or   {id (u/gen-id)}} params]
    [:label {:for   id
             :class (->> (u/names->str [(case checked
                                          (true :checked) :Checked
                                          :indeterminate  :Indeterminate
                                          :Not-Checked)
                                        (:class params)])
                         (str (when-not (some #(= :Toggle %) (:class params)) " Checkbox ")))}
     [:div.Shape
      [:i (when-not (some #(= :Toggle %) (:class params))
            (case checked
              (true :checked) {:class :ion-ios-checkmark-empty}
              :indeterminate  {:class :ion-ios-minus-empty}
              {}))]
      [:input (merge (dissoc params :checked :id :class)
                     {:id      id
                      :type    :checkbox
                      :checked checked})]] label]))


(spec/fdef checkbox
        :args ::checkbox-args
        :ret vector?)
