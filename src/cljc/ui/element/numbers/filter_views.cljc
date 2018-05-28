(ns ui.element.numbers.filter-views
  (:require [re-frame.core :as re-frame]
            [ui.element.menu.views :as menu]
            [ui.element.checkbox.views :refer [checkbox]]
            [ui.element.button.views :refer [button]]
            [ui.element.containers.views :refer [container]]
            [ui.element.searchfield.views :refer [searchfield]]
            [ui.wire.polyglot :refer [translate]]
            [ui.util :as util]))

(defn- string-filter
  "Outputs a list of unique values of a column"
  [id col-ref]
  [:div]
  (let [values     @(re-frame/subscribe [:unique-values id col-ref])
        checkboxes @(re-frame/subscribe [:checkboxes id col-ref])
        filter-smart-case #(re-frame/dispatch [:filter-smart-case id col-ref %1])
        filter-eq  (fn [value] #(re-frame/dispatch [:filter-eq id col-ref value]))]
    [container {:layout :vertically}
     [:small (translate :ui/show-rows-containing)]
     [searchfield {:placeholder (translate :ui/keyword)
                   :on-change   filter-smart-case}]
     [:br]
     (for [value values]
       [checkbox {:key       (util/slug "column-menu" id col-ref value)
                  :on-change (filter-eq value)
                  :checked   (get-in checkboxes [value] false)} value])]))

(defn- date-filter
  "Output a date-picker"
  [id col-ref]
  [:div])

(defn- number-filter
  "Output a range slider with multiple knobs"
  [id col-ref]
  [:div])

(defn column-menu
  "A dropd-down menu for each column of a worksheet for filtering and
  sorting the results"
  [id col-ref]
  (let [column            @(re-frame/subscribe [:column id col-ref])
        visible-column-menu  @(re-frame/subscribe [:state id :show-column-menu])
        sort-ascending    #(re-frame/dispatch [:sort-column id col-ref true])
        sort-descending   #(re-frame/dispatch [:sort-column id col-ref false])]
    [menu/dropdown {:open? (= col-ref visible-column-menu)
                    :origin [:top :right]
                    :on-click-outside #(re-frame/dispatch [:show-column-menu id col-ref])}
     [container {:layout :vertically :gap? false :fill? true :compact? true}
      [button {:key (str col-ref "-sort-asc") :flat true :fill true :class "secondary" :on-click sort-ascending}
       (translate :ui/sort-ascending)]
      [button {:key (str col-ref "-sort-dsc") :flat true :fill true :class "secondary" :on-click sort-descending}
       (translate :ui/sort-descending)]
      [container {:layout :vertically :wrap? false :fill? true :gap? false :style {:max-height "25em" :overflow :auto}}
       (case (:type column)
         :number [number-filter id col-ref]
         :inst   [date-filter id col-ref]
         [string-filter id col-ref])]]]))
