(ns ui.docs.numberfield
  (:require #?(:cljs [reagent.core :refer [atom]])
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as util]))

(defn documentation []
  (let [-max (atom nil)
        -min (atom nil)
        -step (atom nil)
        model (atom nil)]
    (fn []
      [element/article
       "### Number field

       Number fields automatically convert any input into numbers
       "
       [layout/vertically
        [element/numberfield {:label "Max number"
                              :model -max}]
        [element/numberfield {:label "Min number"
                              :model -min}]
        [element/numberfield {:label "Step"
                              :model -step}]
        [element/numberfield {:placeholder "Type in your number here"
                              :max @-max
                              :min @-min
                              :step @-step
                              :model model}]]])))
