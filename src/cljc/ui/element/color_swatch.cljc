(ns ui.element.color-swatch
  (:require [ui.util :as u]
            #?(:cljs [reagent.core :refer [atom]])))


(defn color-swatch
  [hex on-change interactive?]
  (let [!hex         (atom hex)
        local-change #(let [hex (.-value (.-target %))]
                        (reset! !hex hex))]
    (fn []
      (if interactive?
        (do (on-change (str @!hex))
            [:input.Swatch {:style     {:background-color @!hex}
                            :type      :color
                            :value     @!hex
                            :on-change local-change}])
        [:span.Swatch {:style {:background-color @!hex}}]))))
