(ns ui.element.color-swatch.views
  (:require [ui.util :as util]
            [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]))

(defn color-wheel [params]
  (let [{:keys [show-wheel?]} params]
    (when show-wheel? [:div.Color-wheel])))

(defn color-swatch
  [hex on-change interactive?]
  (let [!hex         (atom hex)
        local-change #(let [hex (.-value (.-target %))]
                        (reset! !hex (str hex)))
        show-wheel? (atom false)]
    (fn []
      (let [hex (str @!hex)]
        (on-change hex)
        (if interactive?
          [:div.Swatch
           [color-wheel {:show-wheel? @show-wheel?}]
           [:input {:style     {:background-color hex}
                    :type      :color
                    :value     hex
                    :on-click  #(reset! show-wheel? true)
                    :on-change local-change}]]
          [:span {:style {:background-color hex}}])))))
