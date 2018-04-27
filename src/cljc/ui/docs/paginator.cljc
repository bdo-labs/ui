(ns ui.docs.paginator
  (:require #?(:cljs [reagent.core :refer [atom]])
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.wire.polyglot :as polyglot]
            [ui.util :as util]))

(re-frame/reg-event-db ::page (fn [db [_ page]]
                                (assoc db ::page page)))
(re-frame/reg-sub ::page (fn [db [_]]
                           (::page db)))


(defn documentation []
  (let [model (atom 1)
        length 100
        sub-page (re-frame/subscribe [::page])]
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
        "### Paginator

```clojure
(ns your.namespace
  (:require [reagent.core :as r]
            [ui.elements :as element]))

(def model (r/atom 1))
(def length 100)

[element/paginator {;; required
                    :model model
                    ;; required
                    :length length
                    ;; optional, defaults to 3
                    :edge 3
                    ;; optional, defaults to 10
                    :count-per-page 8
                    ;; optional
                    :on-change (fn [value] (comment \"Takes changed value\"))}]
```"
        [:div "Current page (via model): " [:strong @model]]
        [:div {:style {:margin-bottom "3em"}} "Current page (via subscription and on-change): " [:strong @sub-page]]
        [element/paginator {:model model
                            :length length
                            :on-change (fn [page] (re-frame/dispatch [::page page]))
                            :count-per-page 8}]]])))
