(ns ui.docs.buttons
  (:require [ui.element.button.spec :as button]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.element.showcase.views :refer [showcase]]
            [clojure.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [ui.spec-helper :as spec-helper]
            [ui.wire.polyglot :as polyglot]))

(re-frame/reg-event-db
 ::toggle-controllers
 (fn [db [k value]]
   (assoc-in db [::show-controllers] value)))

(re-frame/reg-sub
 ::show-controllers
 (fn [db path] (get-in db path)))

(defn documentation []
  (let [show-controllers? @(re-frame/subscribe [::show-controllers])
        params {:class "primary"}]
    [layout/vertically {:background :white
                        :gap? false
                        :compact? true
                        :fill?      true}
     [layout/horizontally {:gap? false
                           :compact? true
                           :fill? true}
      [layout/centered {:class "demo"
                        :width 2
                        :fill? true
                        :style {:position :relative
                                :height "35rem"}}
       [:a {:href "https://github.com/bdo-labs/ui/issues/new"
            :target :_blank}
        [element/icon {:title (polyglot/translate :ui/report-issue)
                       :style {:position :absolute
                               :top      "1em"
                               :right    "1em"}} "bug"]]
       [element/button params "Button"]]
      [layout/vertically {:background "rgb(250,250,250)"
                          :gap? false
                          :fill? true
                          :width 1
                          :scrollable? true
                          :style {:border-bottom "solid 1px rgb(235,235,235)"
                                  :height "35rem"
                                  :max-height "35rem"}}
       (let [{:keys [opt-un req-un]} (apply hash-map (rest (spec/form :ui.element.button.spec/params)))
             items (map-indexed (fn [n k] {:id (inc n)
                                           :value (name k)
                                           :label [layout/horizontally {:gap? false}
                                                   [:span (name k)]
                                                   [layout/fill]
                                                   [element/toggle {:checked true} " "]]}) opt-un)]
         [element/collection {:collapsable      true
                              :on-toggle-expand #(re-frame/dispatch [::toggle-controllers %])}
          (apply conj #{{:id    0
                         :value " parameters"
                         :label [layout/horizontally {:gap? false}
                                 [element/icon (str "toggle" (when show-controllers? "-filled"))]
                                 [:span "Parameters"]]}} items)])]]
     [layout/vertically {:fill? true}
      [element/article
       "
## Button

The button-element takes an optional `map` of parameters and arbitrary content.
"]]])
  #_[layout/vertically {:raised? true :background :white :rounded? true
                        :style   {:margin  "2em"
                                  :padding "3em"
                                  :flex    "1 1 80%"}}
     [showcase #'ui.element.button.views/button ::button/args]])

