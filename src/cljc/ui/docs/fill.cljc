(ns ui.docs.fill
  (:require [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as u]))


(re-frame/reg-event-db ::toggle-horizontally? u/toggle)


(re-frame/reg-sub ::horizontally? u/extract-or-false)


(defn documentation
  []
  (let [horizontally?       @(re-frame/subscribe [::horizontally?])
        toggle-horizontally #(re-frame/dispatch [::toggle-horizontally?])]
    [element/article
     "# Fill
     Fill the void in whatever direction it's parent decides"
     [layout/vertically {:fill? true}
      ;; TODO Replace with Radio-buttons
      [element/checkbox {:checked   horizontally?
                         :on-change toggle-horizontally}
       (if horizontally?
         "Horizontally"
         "Vertically")]
      (if horizontally?
        [layout/horizontally {:class "demo" :fill? true}
         [layout/fill]
         [:div.Demo-box "Box"]]
        [layout/vertically {:class "demo" :fill? true}
         [layout/fill]
         [:div.Demo-box "Box"]])]]))
