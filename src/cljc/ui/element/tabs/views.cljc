(ns ui.element.tabs.views
  (:require [clojure.string :as str]
            [ui.element.tabs.spec :as spec]
            [ui.util :as util]))

(defn- render-tab [model on-change {:keys [id label]}]
  (let [label (or label (name id))]
    (fn [_ _]
      [:li {:class (if (= @model id) "active" "")
            :on-click #(do (reset! model id)
                           (when (ifn? on-change) (on-change id)))}
       label])))

(defn tabs
  [& args]
  (let [{:keys [params]}            (util/conform! ::spec/args args)
        {:keys [id on-change render]
         :or   {id (util/gen-id)}}  params
        render (or render :horizontal)]
    (fn [& args]
      (let [{:keys [params]}            (util/conform! ::spec/args args)
            {:keys [style model tabs sheets]
             :or   {style       {}}} params
            -tabs  (util/deref-or-value tabs)]
        [:div.Tabs {:key   (util/slug "tabs" id)
                    :style style
                    :class render}
         [:div.Tabs-wrapper
          [:ul (for [tab -tabs]
                 ^{:key (str id "-" (:id tab))}
                 [render-tab model on-change tab])]]
         (when-let [sheet (get sheets @model)]
           [:div.Sheet
            [sheet]])]))))
