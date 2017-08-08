(ns ui.views
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]
            [ui.readme :as readme]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.docs.centered :as centered]
            [ui.docs.fill :as fill]
            [ui.docs.horizontally :as horizontally]
            [ui.docs.vertically :as vertically]
            [ui.docs.buttons :as buttons]
            [ui.docs.icons :as icons]
            [ui.docs.progress :as progress]
            [ui.docs.colors :as colors]
            [ui.docs.dialog :as dialog]
            [ui.docs.dropdown :as dropdown]
            [ui.docs.inputs :as inputs]
            [ui.docs.date-picker :as date-picker]
            #_[ui.docs.sheet :as sheet]
            #_[ui.docs.sidebar :as sidebar]
            ))


(defn- menu-item []
  (let [active-doc-item (re-frame/subscribe [:active-doc-item])]
    (fn [item]
      [:a {:key (str "menu-item-" (name item))
           :class (if (= @active-doc-item item) "Primary" "")
           :href (str "./#/docs/" (name item))}
       (name item)])))


(defn- intro
  []
  [element/article
   readme/content
   [element/sheet
    {:name           "Release History"
     :caption?       true
     :column-widths  [120 180]}
    [["Released" "Version" "Description"]
     [#inst "2017-06-07"
      [:a.Label {:href "https://github.com/bdo-labs/ui/releases/0.0.1"} "0.0.1"]
      "It's early days, things will break"]]]])


(defn- doc-item
  "Maps the name of a documentation to it's renderer"
  [item-name]
  (case item-name
    ;; Virtuals

    ;; Layouts
    :centered [centered/documentation]
    :fill [fill/documentation]
    :horizontally [horizontally/documentation]
    :vertically [vertically/documentation]

    ;; Elements
    :buttons [buttons/documentation]
    :colors [colors/documentation]
    :date-picker [date-picker/documentation]
    :dialog [dialog/documentation]
    :dropdown [dropdown/documentation]
    :icons [icons/documentation]
    :inputs [inputs/documentation]
    :progress [progress/documentation]
    ;; :sheet [sheet/documentation]
    ;; :sidebar [sidebar/documentation]
    [intro]))


(defn- doc-panel
  []
  (let [active-item @(re-frame/subscribe [:active-doc-item])
        virtuals    [:boundary]
        layouts     [:centered :horizontally :vertically :fill]
        elements    [:buttons :colors :date-picker :dialog :dropdown :icons :inputs :progress]]
    [element/sidebar {:locked true}
     [layout/vertically {:role :navigation}
      [:menu [menu-item :ui]]
      (into [:menu [:h5 "layout/"]] (for [l layouts] [menu-item l])) [:br]
      (into [:menu [:h5 "elements/"]] (for [elem elements] [menu-item elem])) [:br]
      #_(into [:menu [:h5 "virtuals/"]] (for [v virtuals] [menu-item v])) [:br]]
     [doc-item active-item]]))


(defn marketing-panel
  []
  [layout/vertically {:class "Marketing"}
   [layout/centered
    [layout/centered {:raised? true
                      :style   {:background :white}}
     [element/article
      "
      # UI

      ### Hi there! If your looking for a crazy good library for writing front-end `ui`'s, you've come to the right place. 

      "
      [element/button {:class    "primary"
                       :rounded? true} "Get Started"]]]

    [layout/centered
     [element/article
      "
      We've packaged a dozen elements and layouts that will allow you to create production-grade applications without tearing your hair off.


      There's a few things that are quite different about `ui` to other such libraries.

      - There's a layer of guidance both in the docs and in development-mode
      - Layouts are described with verbs. Ex: `layout/vertically`
      - All components are aware of how to generate themselves, so you can quickly create prototypes without having any data on-hand
      "]]]])


(defn- panels [panel-name]
  (case panel-name
    :doc-panel [doc-panel]
    :marketing-panel [marketing-panel]
    [:div]))


(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])
        progress     (re-frame/subscribe [:progress])]
    (fn []
      [:div {:style {:height "100%"}}
       [element/progress-bar {:progress @progress}]
       [panels @active-panel]])))
