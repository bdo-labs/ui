(ns ui.docs.progress
  (:require [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.util :as u]))


(defn documentation []
  (let [progress-bar (re-frame/subscribe [:progress])
        set-progress-bar #(re-frame/dispatch [:set-progress (.-value (.-target %))])
        make-progress #(re-frame/dispatch [:set-progress (+ @progress-bar 10)])]
    [element/article
     "# Progress

     There are a few flavors when it comes to showing of progression

     ### Progress-bar

     The progress-bar is what you would use to show global
     progress. Typically an entire page-load

     Mess with the clamp to make some progress 🤓
     "
     ;; TODO Replace with the clamp-element ones it's working
     [element/clamp {:labels? true
                     :range :upper} (range 100)]
     [:input {:type :number
              :on-change set-progress-bar}]]))
