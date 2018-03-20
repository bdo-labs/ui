(ns ui.views
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.docs.polyglot :as polyglot]
            [ui.docs.load :as load]
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
            [ui.docs.period-picker :as period-picker]
            [ui.docs.sheet :as sheet]))

(defn- menu-item [item]
  (let [active-doc-item @(re-frame/subscribe [:active-doc-item])]
    [:a.Button.nav (merge {:key      (str "menu-item-" (name item))
                           :on-click #(re-frame/dispatch [:navigate :docs (name item)])
                           :href     (str "./docs/" (name item))}
                          (when (= active-doc-item item) {:class "active"}))
     (name item)]))

(defn- intro
  []
  [element/article
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
    ;; Wires
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
    :inputs [inputs/documentation]
    :progress [progress/documentation]
    :sheet [sheet/documentation]
    ;; :sidebar [sidebar/documentation]
    [intro]))

(defn- doc-panel
  []
  (let [active-item @(re-frame/subscribe [:active-doc-item])
        wires       [:load :polyglot]
        layouts     [:centered :horizontally :vertically :fill]
        elements    [:buttons :colors :date-picker :period-picker :dialog :dropdown :icons :inputs :progress :sheet]
        ;; logo-style  {:font-size :8rem :font-weight :bold :text-transform :uppercase :margin 0}
]
    [element/sidebar {:locked true}
     [layout/vertically {:role :navigation}
      [:img {:src "/ui-logo.svg"
             :style {:margin "4rem 0" :width "8rem"}}]
      #_[:menu [:h1 {:style logo-style} [menu-item :ui]]]
      (into [:menu [:h4 "wires/"]] (for [w wires] [menu-item w])) [:br]
      (into [:menu [:h4 "layout/"]] (for [l layouts] [menu-item l])) [:br]
      (into [:menu [:h4 "elements/"]] (for [elem elements] [menu-item elem])) [:br]
      [:br]]
     [doc-item active-item]]))

(defn marketing-panel
  []
  [layout/centered {:class "Marketing"
                    :fill? true
                    :style {:height "100vh"}}
   [element/article
    "
      # UI
      ### A Straight-Forward Library for Composing User-Interfaces
      "
    [element/button {:class    "primary"
                     :on-click #(re-frame/dispatch [:navigate :docs])} "Get Started"]

    "


      #### Why UI?
      - Great interactive documentation
      - Self-aware elements for quick prototyping
      - Describe layouts naturally through verbs
      - Have a say!
      "]])

(defn- not-found-panel []
  [:h1 "Not found"])

(defn- panels [panel-name]
  (case panel-name
    :doc-panel [doc-panel]
    :marketing-panel [marketing-panel]
    :not-found [not-found-panel]
    [:div]))

(defn main-panel []
  (let [active-panel @(re-frame/subscribe [:active-panel])
        fragments    @(re-frame/subscribe [:fragments])]
    [:div {:style {:height "100vh"
                   :width  "100vw"}}
     [panels active-panel]]))
