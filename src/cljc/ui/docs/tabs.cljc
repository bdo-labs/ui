(ns ui.docs.tabs
  (:require #?(:cljs [reagent.core :refer [atom]])
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as util]))

(defn documentation []
  (let [model (atom :one)
        tabs [{:id :one :label "Tab1"}
              {:id :two :label "Tab2"}
              {:id :three :label "Tab3"}]
        sheets {:one (fn [] [:div "One..."])
                :two (fn [] [:div "Two..."])
                :three (fn [] [element/icon {:size 15} "happy-outline"])}]
    (fn []
      [element/article
       "### Tabs"
       [element/tabs {:model model :tabs tabs :sheets sheets :render :vertical}]])))
