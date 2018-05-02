(ns ui.docs.transition
  (:require [clojure.string :as str]
            #?(:cljs [reagent.core :refer [atom]])
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.wire.polyglot :as polyglot]
            [ui.util :as util]))

(defn- transition-states [model [state1 name1 state2 name2]]
  (let [-model @model]
    [:div
     [element/button {:class "primary"
                      :on-click #(reset! model (if (= -model state2) state1 state2))}
      (if (= -model state2)
        name2
        name1)]
     [:br]
     [element/transition {:model model}
      [element/icon {:size 15} "happy-outline"]]]))

(defn documentation []
  (let [fade-model (atom :rest)
        zoom-model (atom :rest)
        flip-horizontal-model (atom :rest)
        flip-vertical-model (atom :rest)
        jiggle-model (atom :rest)]
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
        "### Transition

```clojure
(ns your.namespace
  (:require [reagent.core :as r]

            [ui.elements :as element]))

(def model :play)

[element/transition {;; required
                     :model model
                     ;; for modes that can flip (ie, fade, etc) this parameter decides
                     ;; if it is visible or not
                     :visible? true}
                    ;; the body to be transitioned
                    [element/icon {:size 15} \"happy-outline\"]]
```"
        [transition-states fade-model [:fade-in "Fade in" :fade-out "Fade out"]]
        [transition-states zoom-model [:zoom-in "Zoom in" :zoom-out "Zoom out"]]
        [transition-states flip-horizontal-model [:flip-horizontal-out "Flip horizontal out"
                                                  :flip-horizontal-in  "Flip horizontal in"]]
        [transition-states flip-vertical-model [:flip-vertical-out "Flip vertical out"
                                                :flip-vertical-in  "Flip vertical in"]]
        [transition-states jiggle-model [:jiggle "Jiggle"
                                         :rest "Rest"]]]])))
