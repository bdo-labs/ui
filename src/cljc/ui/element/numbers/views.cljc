(ns ui.element.numbers.views
  (:require [re-frame.core :as re-frame]
            [ui.util :as util]))


(defn- caption [id]
  [:h1 @(re-frame/subscribe [:state id :caption])])


(defn- colgroup []
  [:colgroup])


(defn- table
  [& content]
  (into [:table {:cell-padding 0 :cell-spacing 0}
         [colgroup]] content))


(defn- column-headings [id]
  (when-let [column-heading @(re-frame/subscribe [:state id :column-heading])]
    (when-not (= :hidden column-heading)
     (let [column-references @(re-frame/subscribe [:column-references id])]
       [:tr.Column-headings
        (for [col-ref column-references]
          [:th.Column-heading col-ref])]))))


(defn- title-rows [id]
  (when-let [title-rows @(re-frame/subscribe [:title-rows id])]
    (let [frozen @(re-frame/subscribe [:state id :freeze-title-rows?])]
     [table
      [:thead
       (for [row title-rows]
         [:tr.Title-row
          (for [cell row]
            [:th (:value cell)])])]])))


(defn- table-header [id]
  (let [row-heading    @(re-frame/subscribe [:state id :row-heading])]
    [table
     [:thead
      [column-headings id]
      [caption id]
      [title-rows id]]]))


(defn- table-body [id]
  (let [visible-rows @(re-frame/subscribe [:visible-rows id])]
    [table
     [:tbody
      (for [{:keys [type]
             :as row} visible-rows]
        [:tr.Body-row
         (for [{:keys [value]
                :as cell} row]
           (let [params {:class (name type)}]
             [:td.Body-cell params
              (case type
                :number [:span (format/number value)]
                :inst [:span (format/inst value)]
                [:span value])]))])]]))


(defn sheet [{:keys [hidden] :as params} data]
  (let [id (util/slug (:name params))]
    (re-frame/dispatch [:sheet id params data])
    (fn []
      (when-not hidden
       [:div.Worksheet.fill {:key id}
        [:div.Table
         [:div.Table-Header
          [table-header id]]
         [:div.Table-Body
          [table-body id]]]]))))
