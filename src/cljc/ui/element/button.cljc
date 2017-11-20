(ns ui.element.button
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [clojure.core :refer [atom]]
            [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [garden.units :as unit]
            [garden.color :as color]
            [garden.selectors :as selectors]
            [ui.element.ripple :refer [ripple]]
            [ui.util :as util]))


(defcssfn translateX)
(defcssfn translateY)
(defcssfn scale)


(defn style [{:keys [primary secondary tertiary positive negative]}]
  [[:.Button {:appearance    :none
              :background    (util/gray 245)
              :border-radius (unit/rem 0.5)
              :border        [[:solid (unit/em 0.1) (util/gray 240)]]
              :cursor        :pointer
              :outline       :none
              :user-select   :none
              :min-heigt     (unit/rem 3)
              :min-width     (unit/rem 8)
              :padding       [[(unit/rem 0.25) (unit/rem 1)]]
              ;; FIXME Required by ripple, but ruins z-index
              ;; :position      :relative
              :overflow      :hidden
              :transition    [[:background-color :200ms :ease]]
              :line-height   2.5}
    [:&:hover {:background-color (util/gray 250)}]
    [:&.circular {:border-radius (unit/percent 50)
                  :padding       (unit/rem 0.25)
                  :min-width     (unit/rem 3.5)}
     [:.Icon {:margin-right 0}]]
    [:&.disabled {:opacity 0.3
                  :cursor :not-allowed}]
    [:&.secondary {:background-color secondary
                   :border-color     secondary
                   :color            (if (util/dark? secondary) :white :black)}
     [:&:hover {:background-color (color/lighten secondary 10)}]]
    [:&.primary {:background-color primary
                 :border-color     primary
                 :color            (if (util/dark? primary) :white :black)}
     [:&:hover {:background-color (color/lighten primary 10)}]]
    [:&.tertiary {:background-color tertiary
                  :border-color     tertiary
                  :color            (if (util/dark? tertiary) :white :black)}
     [:&:hover {:background-color (color/lighten tertiary 10)}]]
    [:&.positive {:background-color positive
                  :border-color     positive}]
    [:&.negative {:background-color negative
                  :border-color     negative}]
    [:&.flat {:background-color :transparent
              :border           [[:solid (unit/em 0.1) :inherit]]}]
    [:&.rounded {:border-radius (unit/em 2)}]
    [:.Icon {:font-size      (unit/rem 2)
             :color          :inherit
             :vertical-align :middle
             :margin-right   (unit/rem 1)
             :min-width      (unit/rem 1)
             :line-height    0}]
    [:&.Tab {:background    :transparent
             :border        :none
             :border-bottom [[:solid (unit/px 2) :transparent]]
             :border-radius 0}
     [:&.active {:border-color primary}]]]])


(spec/def ::fill boolean?)
(spec/def ::flat boolean?)
(spec/def ::rounded boolean?)
(spec/def ::circular boolean?)
(spec/def ::content (spec/* (spec/or :str string? :vec vector?)))
(spec/def ::class string?)


(spec/def ::params
  (spec/merge (spec/keys :opt-un [::flat
                                  ::fill
                                  ::rounded
                                  ::circular])
              :ui.element.ripple/params))


(spec/def ::args
  (spec/cat :params ::params
            :content ::content))


(spec/def ::ret
  (spec/cat :element #{:button.Button}
            :params (spec/keys :opt-un [::class])
            :content ::content))


(defn button [& args]
  (let [{:keys [params content]} (util/conform-or-fail ::args args)
        class                    (util/params->classes params)
        ui-params                (conj (util/keys-from-spec ::params) :class)
        params                   (->> (apply dissoc params ui-params)
                                      (merge {:class class}))]
    (into [:button.Button params #_[ripple params]] (map last content))))
