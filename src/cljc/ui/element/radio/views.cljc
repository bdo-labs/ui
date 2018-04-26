(ns ui.element.radio.views
  (:require [clojure.string :as str]
            [ui.element.radio.spec :as spec]
            [ui.util :as util :refer [get-event-handler]]))

(defn- render-button [model on-change top-id {:keys [id label]}]
  (let [button-id (str top-id "-" id)]
   [:li {:class (if (= @model id) "active" "")}
    [:input {:type :radio
             :id button-id
             :on-change #(do (reset! model id)
                             (when (ifn? on-change) (on-change id)))
             :name top-id}]
    (if label
      [:label {:for button-id} label])]))

(defn radio
  [& args]
  (let [{:keys [params]} (util/conform! ::spec/args args)
        {:keys [id on-change buttons id model render]
         :or {id (util/gen-id)}} params
        render (or render :horizontal)]
    (fn [& args]
      (let [{:keys [params]} (util/conform! ::spec/args args)
            {:keys [style]
             :or   {style {}}} params]
        [:div.Radiobuttons {:key (util/slug "radiobutton" id)
                            :style style
                            :class render}
         [:ul
          (for [button buttons]
            (let [-id (str id "-" (:id button))]
              ^{:key -id}
              [render-button model on-change id button]))]]))))
