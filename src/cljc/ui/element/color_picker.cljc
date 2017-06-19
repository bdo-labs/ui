(ns ui.element.color-picker
  (:require [garden.color :as color]
            [ui.util :as u]
            [ui.element.color-swatch :refer [color-swatch]]
            #?(:cljs [reagent.core :refer [atom]])))


(defn color-picker
  [{:keys [hex disabled readonly on-change] :as params}]
  (let [!output     (atom :rgb)
        !clickable? (atom true)
        swap-output #(reset! !output (case @!output :rgb :hsl :hsl :hex :rgb))
        mouse-down  #(reset! !clickable? true)
        mouse-move  #(reset! !clickable? false)
        mouse-up    #(do (when @!clickable? (swap-output))
                         (reset! !clickable? true))
        classes     (u/names->str [:Color-picker
                                 (when (true? disabled) "disabled")
                                 (when (true? readonly) "readonly")])]
    (fn []
      [:div (merge (dissoc params :disabled :readonly :hex)
                   {:class classes})
       [color-swatch hex on-change true]
       [:div.Value {:on-mouse-down mouse-down
                    :on-mouse-move mouse-move
                    :on-mouse-up   mouse-up}
        (let [{r :red g :green b :blue
               :as rgb} (color/hex-str->rgb hex)]
         (case @!output
           :rgb (str "rgb(" r "," g "," b ")")
           :hsl (let [{:keys [h s l]} (color/rgb->hsl r g b)]
                  (str "hsl(" (u/parse-int h) "," (u/parse-int s) "," (u/parse-int l) ")"))
           :hex (str hex)))]])))


;; (spec/def ::disabled boolean?)
;; (spec/def ::readonly boolean?)
;; (spec/def ::color-picker-params-spec
;;   (spec/keys :req-un [::value]
;;           :opt-un [::disabled ::readonly]))
;; (spec/def ::colorspace (spec/int-in 0 256))
;; (spec/def ::red ::colorspace)
;; (spec/def ::green ::colorspace)
;; (spec/def ::blue ::colorspace)
;; ;; (spec/def ::opacity (spec/double-in :min 0 :max 1 :NaN? false))
;; (spec/def ::value
;;   (spec/keys :req-un [::red ::green ::blue]
;;           :opt-un []))
;; (spec/fdef color-picker :args (spec/cat :params ::color-picker-params-spec))
;; (stest/instrument `color-picker)
