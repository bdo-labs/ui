(ns ui.element.color-swatch.views
  (:require [ui.util :as util]
            [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]))


(defn color-swatch
  [hex on-change interactive?]
  (let [!hex         (atom hex)
        local-change #(let [hex (.-value (.-target %))]
                        (reset! !hex (str hex)))]
    (fn []
      (let [hex (str @!hex)]
        (on-change hex)
        (if interactive?
          [:input.Swatch {:style     {:background-color hex}
                          :type      :color
                          :value     hex
                          :on-change local-change}]
          [:span.Swatch {:style {:background-color hex}}])))))
