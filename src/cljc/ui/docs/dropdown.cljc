(ns ui.docs.dropdown
  (:require [ui.elements :as element]
            [ui.layout :as layout]
            #?(:cljs [reagent.core :refer [atom]])))

(defn documentation []
  (let [!open? (atom false)]
    (fn []
      [element/article
       "# Dropdown"
       [layout/vertically
        [element/button {:on-click #(reset! !open? (not @!open?))} "Open"]
        [element/dropdown {:open? @!open?
                           :origin [:top :left]
                           :style {:width "360px"}}
         [layout/horizontally [:h5 "Notifications"]]
         [layout/horizontally {:style {:border-top "1px solid rgb(230,230,230)"
                                       :background "rgb(250,250,250)"}
                               :space :around
                               :fill? true}
          [element/icon {:font "ion"
                         :size "medium"} "ios-information-outline"]
          [:small (str "Some information for you")]
          [:small (str "1 min ago")]]]]])))
