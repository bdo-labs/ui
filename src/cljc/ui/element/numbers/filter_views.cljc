(ns ui.element.numbers.filter-views
  (:require [re-frame.core :as re-frame]
            [ui.element.menu :as menu]
            [ui.element.checkbox :refer [checkbox]]
            [ui.wire.polyglot :refer [translate]]
            [ui.element.button :refer [button]]
            [ui.element.containers :refer [container]]
            [ui.util :as util]))

(defn- string-filter
  "Outputs a list of unique values of a column"
  [id col-ref]
  [:div]
  #_(let [values    @(re-frame/subscribe [:unique-values id col-ref])
          filter-eq #(re-frame/dispatch [:filter-eq id col-ref %])]
      [container {:layout :vertically}
       [:small (translate :ui/show-rows-containing)]
       (for [{:keys [value]} values]
         [checkbox {:key       (util/slug "column-menu" id col-ref value)
                    :on-change filter-eq
                    :checked   false} value])]))

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
        show-column-menu? @(re-frame/subscribe [:show-column-menu? id col-ref])
        sort-ascending    #(re-frame/dispatch [:sort-column id col-ref true])
        sort-descending   #(re-frame/dispatch [:sort-column id col-ref false])]
    [menu/dropdown {:open? show-column-menu?}
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
