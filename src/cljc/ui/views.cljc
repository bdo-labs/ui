(ns ui.views
  (:require [re-frame.core :as re-frame]
            [ui.docs.buttons :as buttons]
            [ui.docs.card :as card]
            [ui.docs.centered :as centered]
            [ui.docs.chooser :as chooser]
            [ui.docs.collection :as collection]
            [ui.docs.colors :as colors]
            [ui.docs.date-picker :as date-picker]
            [ui.docs.dialog :as dialog]
            [ui.docs.dropdown :as dropdown]
            [ui.docs.feature :as feature]
            [ui.docs.fill :as fill]
            [ui.docs.form :as form]
            [ui.docs.horizontally :as horizontally]
            [ui.docs.icons :as icons]
            [ui.docs.inputs :as inputs]
            [ui.docs.load :as load]
            [ui.docs.numberfield :as numberfield]
            [ui.docs.period-picker :as period-picker]
            [ui.docs.polyglot :as polyglot]
            [ui.docs.progress :as progress]
            [ui.docs.sheet :as sheet]
            [ui.docs.textfield :as textfield]
            [ui.docs.vertically :as vertically]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.wire.polyglot :refer [translate]]))

(defn- menu-item
  ([item]
   (menu-item {} item))
  ([params item]
   (let [active-doc-item @(re-frame/subscribe [:active-doc-item])]
     [:a.Button.nav (merge {:key      (str "menu-item-" (name item))
                            :on-click #(re-frame/dispatch [:navigate (name item)])
                            :href     (str "./" (name item))}
                           (when (= active-doc-item item) {:class "active"})
                           params)
      (name item)])))

(defn- intro
  []
  [element/article
   "
# A Straight-Forward Library for Composing User-Interfaces

UI is geared towards Clojure(Script) applications and more
specifically, reagent/re-frame applications. There are a lot of
batteries included, so have a look and get yourself familiarized.
"
   [layout/vertically
    [:h2 "Changelog"]
    [:h3 "0.0.1" [:span {:style {:color :darkgray :margin-left "1em" :display :inline-block}} (translate :ui/date-full  #inst "2016-07-11T22:31:00+06:00")]]
    [layout/horizontally {:gap? false}
     [element/badge {} "CHANGED"]
     [:span "Everything"]]]])

(defn- doc-item
  "Maps the name of a documentation to it's renderer"
  [item-name]
  (case item-name
    ;; Wires
    :form     [form/documentation]
    :feature  [feature/documentation]
    :load     [load/documentation]
    :polyglot [polyglot/documentation]

    ;; Layouts
    :centered [centered/documentation]
    :fill [fill/documentation]
    :horizontally [horizontally/documentation]
    :vertically [vertically/documentation]

    ;; Elements
    :buttons [buttons/documentation]
    :colors [colors/documentation]
    :date-picker [date-picker/documentation]
    :period-picker [period-picker/documentation]
    :dialog [dialog/documentation]
    :dropdown [dropdown/documentation]
    :icons [icons/documentation]
    :textfield [textfield/documentation]
    :chooser [chooser/documentation]
    :collection [collection/documentation]
    :inputs [inputs/documentation]
    :numberfield [numberfield/documentation]
    :progress [progress/documentation]
    :sheet [sheet/documentation]
    ;; :sidebar [sidebar/documentation]

    ;; Labs
    :card [card/documentation]
    [intro]))

(defn- doc-panel
  []
  (let [active-item @(re-frame/subscribe [:active-doc-item])
        wires       [:feature :form :load :polyglot]
        layouts     [:centered :horizontally :vertically :fill]
        elements    [:buttons :colors #_:date-picker #_:period-picker :dialog :dropdown :icons :textfield :numberfield :collection #_:chooser :inputs :progress :sheet]
        labs        [:card]]
    [:div {:style {:width "100vw"
                   :height "100vh"}}
     [element/header {:background "rgb(65,88,208)"
                      :style {:color :white}}
      [:span]
      [:a {:href "https://github.com/bdo-labs/ui/"
           :target :_blank
           :style {:color :white}} [element/icon {:size 3} "social-github"]]]
     [element/sidebar {:locked? true :open? true}
      [layout/vertically {:role :navigation}
       (into [:menu [:h4 "wires/"]] (for [w wires] [menu-item w])) [:br]
       (into [:menu [:h4 "layout/"]] (for [l layouts] [menu-item l])) [:br]
       (into [:menu [:h4 "elements/"]] (for [elem elements] [menu-item elem])) [:br]
       (into [:menu [:h4 "lab/"]] (for [lab labs] [menu-item lab])) [:br]
       [:br]]
      [doc-item active-item]]]))

(defn- not-found-panel []
  [:h1 "Not found"])

(defn- panels [panel-name]
  (case panel-name
    :doc-panel [doc-panel]
    :not-found [not-found-panel]
    [:div]))

(defn main-panel []
  (let [active-panel @(re-frame/subscribe [:active-panel])
        fragments    @(re-frame/subscribe [:fragments])]
    [:div {:style {:height "100vh"
                   :width  "100vw"}}
     [panels active-panel]]))
