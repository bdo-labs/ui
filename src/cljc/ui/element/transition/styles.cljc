(ns ui.element.transition.styles
  (:require #?(:clj [garden.def :refer [defkeyframes]])
            [garden.units :as unit]
            [garden.color :as color]
            [clojure.string :as str]))



(defn style [_]
  [[:.Transition        {:display :inline-block}

    ;; fade
    [:&.fade-in         {:animation [[:200ms :fade-in :ease-in-out :forwards]]}]
    [:&.fade-out        {:animation [[:200ms :fade-out :ease-in-out :forwards]]}]
    ;; zoom
    [:&.zoom-in         {:animation [[:200ms :zoom-in :ease-in-out :forwards]]}]
    [:&.zoom-out        {:animation [[:200ms :zoom-out :ease-in-out :forwards]]}]
    ;; flip
    [:&.flip-horizontal-in  {:animation [[:200ms :flip-horizontal-in :ease-in-out :forwards]]}]
    [:&.flip-horizontal-out {:animation [[:200ms :flip-horizontal-out :ease-in-out :forwards]]}]
    [:&.flip-vertical-in    {:animation [[:200ms :flip-vertical-in :ease-in-out :forwards]]}]
    [:&.flip-vertical-out   {:animation [[:200ms :flip-vertical-out :ease-in-out :forwards]]}]

    ;; static animations
    [:&.jiggle              {:animation [[:200ms :jiggle :ease-in-out]]}]]])
