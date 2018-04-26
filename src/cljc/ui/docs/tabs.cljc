(ns ui.docs.tabs
  (:require #?(:cljs [reagent.core :refer [atom]])
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.wire.polyglot :as polyglot]
            [ui.util :as util]))

(defn show-sheet [sheets model]
  (if-let [sheet (get sheets @model)]
    [sheet]))

(defn documentation []
  (let [model (atom :one)
        tabs [{:id :one :label "Tab1"}
              {:id :two :label "Tab2"}
              {:id :three :label "Tab3"}]
        sheets {:one (fn [] [:div "One..."])
                :two (fn [] [:div "Two..."])
                :three (fn [] [element/icon {:size 15} "happy-outline"])}]
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
       [layout/horizontally
        [element/article
         "### Tabs

```clojure
(ns your.namespace
  (:require [reagent.core :as r]
            [ui.elements :as element]))

(def model (r/atom :one))
(def tabs  [{:id :one :label \"Tab1\"}
            {:id :two :label \"Tab2\"}
            {:id :three :label \"Tab3\"}])
(def sheets {:one (fn [] [:div \"One...\"])
             :two (fn [] [:div \"Two...\"])
             :three (fn [] [element/icon {:size 15} \"happy-outline\"])})

[element/tabs {;; required
               :model model
               ;; required
               :tabs tabs
               ;; optional
               :sheets sheets
               ;; optional, defaults to :horizontal
               :render :horizontal}]
```

#### :horizontal"
         [element/tabs {:model model :tabs tabs :sheets sheets :render :horizontal}]
         "#### :vertical"
         [element/tabs {:model model :tabs tabs :render :vertical}]
         "#### :horizontal-bars"
         [element/tabs {:model model :tabs tabs :render :horizontal-bars}]
         "#### :vertical-bars"
         [element/tabs {:model model :tabs tabs :render :vertical-bars}]]
        [element/article
         "### Sheet"
         [show-sheet sheets model]]]])))
