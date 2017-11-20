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
            #_[ui.docs.progress :as progress]
            [ui.docs.colors :as colors]
            [ui.docs.dialog :as dialog]
            [ui.docs.dropdown :as dropdown]
            [ui.docs.inputs :as inputs]
            [ui.docs.date-picker :as date-picker]
            [ui.docs.sheet :as sheet]
            #?(:cljs [ui.docs.boundary :as boundary])))


(defn- menu-item []
  (let [active-doc-item (re-frame/subscribe [:active-doc-item])]
    (fn [item]
      [:a {:key      (str "menu-item-" (name item))
           :on-click #(re-frame/dispatch [:navigate :docs (name item)])
           :class    (if (= @active-doc-item item) "face-primary" "face-tertiary")
           :href     (str "./docs/" (name item))}
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
    :boundary #?(:clj [] :cljs [boundary/documentation])

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
    ;; :progress [progress/documentation]
    :sheet [sheet/documentation]
    ;; :sidebar [sidebar/documentation]
    [intro]))


(defn- doc-panel
  []
  (let [active-item @(re-frame/subscribe [:active-doc-item])
        virtuals #?(:clj [] :cljs [:boundary])
        layouts     [:centered :horizontally :vertically :fill]
        elements    [:buttons :colors :date-picker :dialog :dropdown :icons :inputs :sheet]]
    [element/sidebar {:locked true}
     [layout/vertically {:role :navigation}
      [:menu [menu-item :ui]]
      (into [:menu [:h4 "layout/"]] (for [l layouts] [menu-item l])) [:br]
      (into [:menu [:h4 "elements/"]] (for [elem elements] [menu-item elem])) [:br]
      #?(:cljs (into [:menu [:h4 "virtuals/"]] (for [v virtuals] [menu-item v])))
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
                     :on-click #(re-frame/dispatch [:navigate :docs])
                     :rounded  true} "Get Started"]

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
        ;; progress     @(re-frame/subscribe [:progress])
        fragments    @(re-frame/subscribe [:fragments])]
    [:div {:style {:height "100vh"
                   :width  "100vw"}}
     #_[element/progress-bar {:progress progress}]
     [panels active-panel]]))
