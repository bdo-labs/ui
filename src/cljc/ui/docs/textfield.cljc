(ns ui.docs.textfield
  (:require [ui.element.textfield.spec :as spec]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.element.showcase.views :refer [showcase]]))

(defn documentation []
  [layout/vertically {:background  :white
                      :scrollable? true}
   [showcase #'ui.element.textfield.views/textfield ::spec/args]])

