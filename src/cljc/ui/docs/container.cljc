(ns ui.docs.container
  (:require [re-frame.core :refer [subscribe dispatch reg-sub reg-event-db]]
            #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [ui.layout :as layout]
            [ui.elements :as element]))


(reg-event-db ::set-direction (fn [db [_ direction]] (assoc db ::direction direction)))
(reg-event-db ::set-compact (fn [db [_ compact?]] (assoc db ::compact compact?)))
(reg-event-db ::set-no-gap (fn [db [_ no-gap]] (assoc db ::no-gap no-gap)))
(reg-event-db ::set-fill (fn [db [_ fill]] (assoc db ::fill fill)))
(reg-event-db ::set-align (fn [db [_ align]] (assoc db ::align align)))
(reg-event-db ::set-justify (fn [db [_ align]] (assoc db ::justify align)))
(reg-event-db ::set-first-title (fn [db [_ title]] (assoc db ::first-title title)))
(reg-event-db ::set-second-title (fn [db [_ title]] (assoc db ::second-title title)))
(reg-event-db ::set-last-title (fn [db [_ title]] (assoc db ::last-title title)))


(reg-sub ::direction (fn [db] (::direction db)))
(reg-sub ::align (fn [db] (::align db)))
(reg-sub ::justify (fn [db] (::justify db)))
(reg-sub ::compact (fn [db] (::compact db)))
(reg-sub ::no-gap (fn [db] (::no-gap db)))
(reg-sub ::fill (fn [db] (::fill db)))
(reg-sub ::first-title (fn [db [_ title]] (or (::first-title db) title)))
(reg-sub ::second-title (fn [db [_ title]] (or (::second-title db) title)))
(reg-sub ::last-title (fn [db [_ title]] (or (::last-title db) title)))


(defn panel []
  (fn []
    (let [direction    @(subscribe [::direction])
          align        @(subscribe [::align])
          justify      @(subscribe [::justify])
          compact      @(subscribe [::compact])
          no-gap       @(subscribe [::no-gap])
          first-title  @(subscribe [::first-title "Foo"])
          second-title @(subscribe [::second-title "Bar"])
          last-title   @(subscribe [::last-title "Baz"])
          fill         @(subscribe [::fill])]
      [layout/vertically
       [:header.Fill {:role :banner}
        [:h1 "Container"]
        [:p.Copy
         (str "The container-element is what's used behind the scenes for most of the layout's."
              "As such, you could think of a container as an advanced-mode.")
         [:br]
         [:strong "Thus; if a layout will do the job, don't use a container."]]]
       [element/container {:direction direction
                           :compact   compact
                           :align     align
                           :justify   justify
                           :no-gap    no-gap
                           :class     [:demo]
                           :style     {:min-height "35vh"}
                           :fill      fill}
        [:div.Demo-box first-title]
        [:div.Demo-box second-title]
        [:div.Demo-box last-title]]
       [layout/horizontally {:no-gap true}
        [element/code {:class [:fill]}
         [:span.Parens "["] [:span.Symbol "element/container"] [:span.Parens " {"] [:span.Keyword ":direction"] (str " \"") [:label {:for (if (= direction "row") "column" "row")} [:strong (str direction)]] (str "\"
                    ") [:span.Keyword ":align"] (str "     \"") [:label {:for :align} [:strong (str align)]] (str "\"
                    ") [:span.Keyword :justify] (str "    \"") [:label {:for :justify} [:strong (str justify)]] (str "\"
                    ") [:span.Keyword :compact] (str "    \"") [:label {:for "compact"} [:strong (str compact)]] (str "\"
                    ") [:span.Keyword :no-gap] (str "     \"") [:label {:for "no-gap"} [:strong (str no-gap)]] (str "\"
                    ") [:span.Keyword :fill] (str "       \"") [:label {:for "fill"} [:strong (str fill)]] (str "\"") [:span.Parens "}
             "] [:span.Parens "["] [:span.Keyword ":div.Demo-box"] (str " \"") [:span {:content-editable true :on-input #(dispatch [::set-first-title (.-innerHTML (.-target %))])} "Foo"] (str "\"") [:span.Parens "]
             "] [:span.Parens "["] [:span.Keyword ":div.Demo-box"] (str " \"") [:span {:content-editable true :on-input #(dispatch [::set-second-title (.-innerHTML (.-target %))])} "Bar"] (str "\"") [:span.Parens "]
             "] [:span.Parens "["] [:span.Keyword ":div.Demo-box"] (str " \"") [:span {:content-editable true :on-input #(dispatch [::set-last-title (.-innerHTML (.-target %))])} "Baz"] (str "\"") [:span.Parens "]]"]]
        [:div.Functional-hide
         [:select#justify {:on-change #(dispatch [::set-justify (.-value (.-target %))])}
          [:option {:value "start"} "Start"]
          [:option {:value "end"} "End"]
          [:option {:value "center"} "Center"]
          [:option {:value "space-between"} "Space between"]
          [:option {:value "space-around"} "Space around"]]
         [:select#align {:on-change #(dispatch [::set-align (.-value (.-target %))])}
          [:option {:selected (= "start" align) :value "start"} "Start"]
          [:option {:selected (= "end" align) :value "end"} "End"]
          [:option {:selected (= "stretch" align) :value "stretch"} "Stretch"]
          [:option {:selected (= "center" align) :value "center"} "Center"]]
         [:input#row {:name "direction" :checked (= "row" direction) :type :radio :value "row" :on-click #(dispatch [::set-direction "row"])}]
         [:input#column {:name "direction" :checked (= "column" direction) :type :radio :value "column" :on-click #(dispatch [::set-direction "column"])}]
         [:input#compact {:name "compact" :type :checkbox :value "compact" :on-click #(dispatch [::set-compact (not compact)])}]
         [:input#no-gap {:name "no-gap" :type :checkbox :value "no-gap" :on-click #(dispatch [::set-no-gap (not no-gap)])}]
         [:input#fill {:name "fill" :type :checkbox :value "fill" :on-click #(dispatch [::set-fill (not fill)])}]]]])))
