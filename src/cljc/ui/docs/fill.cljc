(ns ui.docs.fill
  (:require [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as util]))

(re-frame/reg-event-db ::toggle-horizontally? util/toggle)

(re-frame/reg-sub ::horizontally? util/extract-or-false)

(defn documentation
  []
  (let [horizontally?       @(re-frame/subscribe [::horizontally?])
        toggle-horizontally #(re-frame/dispatch [::toggle-horizontally?])]
    [layout/vertically {:background :white :gap? false :fill? true}

     (if horizontally?
       [layout/horizontally {:class "demo fill-demo" :fill? true}
        [layout/fill]
        [:div.Demo-box "Box"]]
       [layout/vertically {:class "demo fill-demo" :fill? true}
        [layout/fill]
        [:div.Demo-box "Box"]])
     [element/article
      "
## Fill

Fill the void in whatever direction it's parent decides"
      [:div
       [:span {:style {:display :inline-block :margin-right "1em"}} "Vertically"]
       [element/toggle {:checked   horizontally?
                        :on-change toggle-horizontally}
        " "]
       [:span "Horizontally"]]]]))
