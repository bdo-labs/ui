(ns ui.views
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]
            [ui.readme :as readme]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as u]
            [ui.docs.centered :as centered]
            [ui.docs.fill :as fill]
            [ui.docs.horizontally :as horizontally]
            [ui.docs.vertically :as vertically]
            [ui.docs.colors :as colors]
            [ui.docs.dialog :as dialog]
            [ui.docs.buttons :as buttons]
            [ui.docs.inputs :as inputs]
            [ui.docs.sheet :as sheet]
            [ui.docs.sidebar :as sidebar]))


(defn- menu-item []
  (let [active-doc-item (re-frame/subscribe [:active-doc-item])]
    (fn [item]
      [:a {:key (str "menu-item-" (name item))
           :class (if (= @active-doc-item item) "Primary" "")
           :href (str "/#/" (name item))}
       (name item)])))


(defn- footer []
  [:footer
   [layout/horizontally {:no-gap true :class [:legal]}
    [:span (str "© BDO 2017")]
    [:a {:href "//bdo-labs.github.io/"} (str "Blog")]
    [:a {:href ""} (str "Terms Of Service")]
    [:a {:href ""} (str "Privacy Policy")]]])


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
    :dialog [dialog/documentation]
    :inputs [inputs/documentation]
    :sheet [sheet/documentation]
    :sidebar [sidebar/documentation]
    [intro]))


(defn- doc-panel
  []
  (let [active-item @(re-frame/subscribe [:active-doc-item])
        virtuals    (sort [:boundary])
        layouts     (sort [:centered :fill :horizontally :vertically])
        elements    (sort [:buttons :colors :dialog :inputs :sheet :sidebar])]
    [element/sidebar {:locked true}
     [layout/vertically
      [:menu [menu-item :ui]]
      (into [:menu [:h5 "layout/"]] (for [l layouts] [menu-item l])) [:br]
      (into [:menu [:h5 "elements/"]] (for [elem elements] [menu-item elem]))]
     [doc-item active-item]]))


(defn- panels [panel-name]
  (case panel-name
    :doc-panel [doc-panel]
    [:div]))


(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [panels @active-panel])))
