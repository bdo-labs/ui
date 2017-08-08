(ns ui.docs.progress
  (:require [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.util :as u]))


(defn documentation []
  (let [progress-bar (re-frame/subscribe [:progress])
        set-progress-bar #(re-frame/dispatch [:set-progress %])
        make-progress #(re-frame/dispatch [:set-progress (+ @progress-bar 10)])
        on-change #(set-progress-bar (:min %))]
    [element/article
     "### Progress

     There are a few flavors when it comes to showing progression

     ### Progress-bar

     The progress-bar is what you would use to show global-
     progress. Typically an entire page-load

     Mess with the clamp to make some progress 🤓
     "
     [element/clamp {:id "progress-clamp"
                     :labels? false
                     :range :lower
                     :on-change on-change} (range 101)]
     "### Spinner
     "
     [element/spinner]]))
