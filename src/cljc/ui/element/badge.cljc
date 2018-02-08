(ns ui.element.badge
  (:require [ui.util :as util]
            [clojure.spec.alpha :as spec]
            [garden.units :as unit]
            [garden.color :as color]))


(defn badge [& args]
  (let [{:keys [params content]} (util/conform! ::args args)
        {:keys [show-count class]
         :or   {show-count true
                class      ""}}  params
        ui-params                (util/keys-from-spec ::params)
        class                    (util/params->classes params)]
    (when (> content 0)
      [:div.Badge (merge {:class class}
                         (apply dissoc params (conj ui-params :class)))
       (when show-count content)])))


(defn style [{:keys [primary secondary]}]
  [[:.Badge {:background    (color/lighten primary 15)
             :border-radius (unit/em 1)
             :color         (color/darken primary 15)
             :display       :block
             :padding       (unit/px 2)
             :min-height    (unit/px 3)
             :min-width     (unit/px 3)
             :font-size     (unit/px 10)
             :position      :absolute
             :z-index       99
             :animation     [[:scaled :200ms :ease]]}
    [:&.show-count {:min-width   (unit/px 6)
                   :height      (unit/px 6)
                   :line-height (unit/px 7)
                   :padding     (unit/em 0.5)}]]])


(spec/def ::show-count boolean?)


(spec/def ::params
  (spec/keys :opt-un [::show-count]))


(spec/def ::content
  (spec/nonconforming
   (spec/or :num nat-int?
            :nil nil?)))


(spec/def ::args
  (spec/cat :params (spec/? ::params)
            :content ::content))
