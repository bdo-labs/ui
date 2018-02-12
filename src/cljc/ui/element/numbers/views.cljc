(ns ui.element.numbers.views
  (:require [re-frame.core :as re-frame]
            [ui.util :as util]))


(defn- caption [id]
  [:h1 @(re-frame/subscribe [:state id :caption])])


(defn- colgroup []
  [:colgroup
   ])


(defn- table
  [& content]
  (into [:table {:cell-padding 0 :cell-spacing 0}
         [colgroup]] content))


(defn- title-rows [id]
  (let [title-rows @(re-frame/subscribe [:title-rows id])]
   [:thead
    [:tr [:td]]]))


(defn- table-body [id]
  (let [rows @(re-frame/subscribe [:rows id])]
    [:tbody
     (for [row rows]
       [:pre row])]))


(defn sheet [{:keys [hidden] :as params} data]
  (let [id (util/slug (:name params))]
    (re-frame/dispatch [:sheet id params data])
    (fn []
      (when-not hidden
       [:div.Worksheet.fill {:key id}
        [:div.Table
         [:div.Table-Header
          [title-rows id]]
         [:div.Table-Body
          [table-body id]]]]))))
