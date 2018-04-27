(ns ui.docs.breadcrumbs
  (:require #?(:cljs [cljs.reader :refer [read-string]]
               :clj  [clojure.edn :refer [read-string]])
            [clojure.string :as str]
            #?(:cljs [reagent.core :refer [atom]])
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.wire.polyglot :as polyglot]
            [ui.util :as util]))

(re-frame/reg-event-db ::text (fn [db [_ text]]
                                (assoc db ::text text)))

(re-frame/reg-sub ::text (fn [db _]
                           #?(:cljs
                              (let [text (::text db)]
                                (if (and (string? text)
                                         (str/starts-with? text "["))
                                  (try (read-string text)
                                       (catch js/Error _))
                                  text)))))


(defn documentation []
  (let [initial-value "[[\"foo\" \"Foo\"] [\"bar\" \"Bar\"] [\"baz\" \"Baz\"]]"
        model (atom initial-value)
        model-breadcrumbs (re-frame/subscribe [::text])]
    (re-frame/dispatch [::text initial-value])
    (fn []
      (let [-model @model]
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
          "### Breadcrumbs

```clojure
(ns your.namespace
  (:require [reagent.core :as r]
            [ui.elements :as element]))

;; alternative 1, first element is part of the URI, second element is the display
(def model [[\"foo\" \"Foo\"] [\"bar\" \"Bar\"] [\"baz\" \"Baz\"]])
;; alternative 2
(def model \"/foo/bar/baz\")

[element/breadcrumbs {;; required
                      :model model}]
```"

          "**Change your path here**"
          [element/textfield {:model model :placeholder "Change the path here"
                              :on-change #(re-frame/dispatch [::text %])}]
          [:br]
          [:br]
          [element/breadcrumbs {:model model-breadcrumbs}]]]))))
