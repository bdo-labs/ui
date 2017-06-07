(ns ui.element.color-swatch)


(defn color-swatch
  [hex on-change interactive?]
  (let [!hex            (atom hex)
        local-change #(let [hex (.-value (.-target %))]
                           (reset! !hex hex)
                           (on-change (str hex)))]
    (fn []
      (if interactive?
        [:input.Swatch {:style {:background-color @!hex}
                        :type :color
                        :value @!hex
                        :on-change local-change}]
        [:span.Swatch {:style {:background-color @!hex}}]))))
