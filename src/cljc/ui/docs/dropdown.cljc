(ns ui.docs.dropdown
  (:require [ui.elements :as element]
            [ui.layout :as layout]
            #?(:cljs [reagent.core :refer [atom]])
            [ui.util :as util]))

(defn documentation []
  (let [!open? (atom false)]
    (fn []
      [element/article
       "### Dropdown

       Drop-downs are great for hiding auxiliary actions.
       "
       [layout/vertically
        [element/button {:class "primary"
                         :circular true
                         :title "Notifications"
                         :on-click #(reset! !open? (not @!open?))}
         [element/icon "ios-bell-outline"]]
        [element/dropdown {:open? @!open?
                           :origin [:top :right]
                           :style {:width "360px"}}
         [layout/horizontally [:h5 "Notifications"]]
         [layout/horizontally {:style {:border-top "1px solid rgb(230,230,230)"
                                       :background "rgb(250,250,250)"}
                               :space :around
                               :fill? true}
          [element/icon {:font "ion"
                         :size 2} "ios-information-outline"]
          [:small (str "Some information for you")]
          [:small (str "1 min ago")]]]]
       "
       You can use any arbitrary element you'd like inside of a
       drop-down. The drop-down only supplies a way of toggling
       content in and out of the view from a certain position.
       "])))
