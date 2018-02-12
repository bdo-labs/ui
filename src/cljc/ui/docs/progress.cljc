(ns ui.docs.progress
  (:require [ui.elements :as element]))


(defn documentation []
  [element/article
   "### Progress

     There are a few flavors when it comes to showing progression

     ### Progress-bar

     The progress-bar is what you would use to show global-
     progress. Typically an entire page-load

     Mess with the clamp to make some progress 🤓
     "
   #_[element/clamp {:id        "progress-clamp"
                     :labels?   false
                     :range     :lower
                     :on-change on-change} (range 101)]
   "### Spinner
     "
   [element/spinner]])
