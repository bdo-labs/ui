(ns ui.element.button
  (:require #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [clojure.string :as str]
            [garden.units :as unit]
            [garden.color :as color]
            [ui.util :as u]))


(defn style [{:keys [primary secondary positive negative]}]
  [[:.Button {:appearance     :none
              :background     (u/gray 230)
              :border-radius  (unit/rem 0.5)
              :border         [[:solid (unit/em 0.1) (u/gray 215)]]
              :outline        :none
              :user-select    :none
              :min-width      (unit/rem 8)
              :text-transform :uppercase
              :transition     [[:background-color :200ms :ease]]
              :line-height    2.5}
    [:&:hover {:background-color (u/gray 240)}]
    [:&.circular {:border-radius (unit/percent 50)
                  :min-width     (unit/rem 3.5)}]
    [:&.secondary {:background-color secondary
                   :border-color     secondary
                   :color            (if (u/dark? secondary) :white :black)}
     [:&:hover {:background-color (color/lighten secondary 10)}]]
    [:&.primary {:background-color primary
                 :border-color     primary
                 :color            (if (u/dark? primary) :white :black)}
     [:&:hover {:background-color (color/lighten primary 10)}]]
    [:&.positive {:background-color positive
                  :border-color     positive}]
    [:&.negative {:background-color negative
                  :border-color     negative}]
    [:&.flat {:background-color :transparent
              :border           [[:solid (unit/em 0.1) :inherit]]}]
    [:&.rounded {:border-radius (unit/em 2)}]
    [:.Icon {:font-size   (unit/rem 2)
             :vertical-align :middle
             :min-width   (unit/rem 1)
             :line-height 0}]]])


(spec/def ::flat? boolean?)
(spec/def ::rounded? boolean?)
(spec/def ::circular? boolean?)
(spec/def ::content (spec/* (spec/or :str string? :vec vector?)))
(spec/def ::class string?)


(spec/def ::params
  (spec/keys :opt-un [::flat?
                      ::rounded?
                      ::circular?]))


(spec/def ::args
  (spec/cat :params ::params
            :content ::content))


(spec/def ::ret
  (spec/cat :element #{:button.Button}
            :params (spec/keys :opt-un [::class])
            :content ::content))


(defn button [& args]
  (let [{:keys [params content]} (u/conform-or-fail ::args args)
        class                    (u/params->classes params)
        ui-params                (conj (u/keys-from-spec ::params) :class)
        params                   (->> (apply dissoc params ui-params)
                                      (merge {:class class}))]
    (into [:button.Button params] (map last content))))


#_(spec/fdef button
           :args ::args
           :ret ::ret
           :fn (fn [{{element :element params :params content :content} :args ret :ret}]
                 (into [element params] content)))



#_(defn button
  ([content]
   [button {} content])
  ([{:keys [fill? flat? rounded? class] :as params} & content]
   (let [classes (u/names->str (concat [(when flat? :flat)
                                        (when fill? :fill)
                                        (when rounded? :rounded)] class))]
     (into [:button.Button
            (merge (dissoc params
                           :fill?
                           :flat?
                           :rounded?) {:class classes})]
           content))))
