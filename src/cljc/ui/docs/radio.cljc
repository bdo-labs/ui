(ns ui.docs.radio
  (:require #?(:cljs [reagent.core :refer [atom]])
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.wire.polyglot :as polyglot]
            [ui.util :as util]))



(defn documentation []
  (let [model (atom :one)
        buttons [{:id :one :label "Radio button one"}
                 {:id :two :label "Radio button two"}
                 {:id :three :label "Radio button three"}]]
    (fn []
      [layout/vertically {:style {:padding "0"}
                          :background :white
                          :gap? false}
       [:a {:href "https://github.com/bdo-labs/ui/issues/new"
            :target :_blank}
        [element/icon {:title (polyglot/translate :ui/report-issue)
                       :style {:position :absolute
                               :top      "1em"
                               :right    "1em"}} "bug"]]
       [element/article
        "### Radio buttons

```clojure
(ns your.namespace
  (:require [reagent.core :as r]
            [ui.elements :as element]))

(def model (r/atom :one))
(def buttons [{:id :one :label \"Radio button one\"}
              {:id :two :label \"Radio button two\"}
              {:id :three :label \"Radio button three\"}]

[element/radio {;; required
                :model model
                ;; required
                :buttons buttons
                ;; optional, defaults to :horizontal
                :render :horizontal
                ;; optional
                :on-change (fn [value] (comment \"Takes changed value\"))}]
```"
        [:span "Current value is " [:strong (str @model)]]
        "#### :horizontal"
        [element/radio {:model model :buttons buttons :render :horizontal}]
        "#### :vertical"
        [element/radio {:model model :buttons buttons :render :vertical}]]])))
