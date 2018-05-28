(ns ui.element.numbers.views
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [re-frame.core :as re-frame]
            #?(:cljs [reagent.core :as reagent])
            [ui.element.progress-bar.views :refer [progress-bar]]
            [ui.element.numbers.filter-views :refer [column-menu]]
            [ui.element.numbers.subs]
            [ui.element.numbers.events]
            [ui.element.icon.views :refer [icon]]
            [ui.element.loaders.views :refer [spinner]]
            [ui.element.chooser.views :refer [chooser]]
            [ui.element.textfield.views :refer [textfield]]
            [ui.util :as util]
            [clojure.core.async :as async :refer [<! #?(:clj go) timeout]]
            [clojure.string :as str]))

(defn hidden?
  [id col-ref]
  (let [hide @(re-frame/subscribe [:state id :hide-columns])]
    (contains? hide col-ref)))

(defn- colgroup [id]
  (let [column-widths @(re-frame/subscribe [:column-widths id])]
    (into [:colgroup]
          (doall
           (->> column-widths
                (map-indexed
                 (fn [n width]
                   [:col {:key (util/slug id "col" n)
                          :width width}])))))))

(defn- table
  [id params & content]
  (into [:table (merge {:cell-padding 0 :cell-spacing 0} params)
         [colgroup id]] content))

(defn- table-header [id]
  (let [title-rows         @(re-frame/subscribe [:visible-title-rows id])
        sort-ascending?    @(re-frame/subscribe [:sort-ascending? id] {})
        sorted-column      @(re-frame/subscribe [:sorted-column id])
        filters            @(re-frame/subscribe [:filters id])
        toggle-column-menu (fn [col-ref]
                             #(go (<! (timeout 20))
                                  (re-frame/dispatch [:show-column-menu id col-ref])))
        hidden?            (partial hidden? id)]
    #_[caption id]
    [:thead
     #_[column-headings id]
     (doall
      (->> title-rows
           (map-indexed
            (fn [n row]
              [:tr.Title-row {:key (util/slug id "title" "row" n)
                              :class (str/join " " [(util/slug "title-row" n)])}
               (doall
                (for [{:keys [value cell-ref col-ref]} row]
                  (when (not (hidden? col-ref))
                    [:th.Title-column {:key   (util/slug id "title" cell-ref)
                                       :class (str/join " " [(util/slug "cell" cell-ref) (util/slug "col" col-ref)])
                                       :style {:border-top "1px solid rgb(230,230,230)"}}
                     [:span value]
                     (when (seq (nth filters (util/col-num cell-ref)))
                       [icon {:style {:margin-left "1em"}
                              :size 1.25
                              :color "rgb(150,150,150)"} "funnel"])
                     (when (= sorted-column (util/col-ref cell-ref))
                       [:span.Arrow (if sort-ascending? "↑" "↓")])
                     [:span
                      [:button.Dropdown-origin
                       {:on-click (toggle-column-menu col-ref)} "›"]
                      [column-menu id col-ref]]])))]))))]))

(defn- table-body-row [id n]
  (let [column-count @(re-frame/subscribe [:column-count id])
        row          @(re-frame/subscribe [:row id n])
        row-height   @(re-frame/subscribe [:state id :row-height])
        editable?    @(re-frame/subscribe [:state id :editable?])
        editing      @(re-frame/subscribe [:state id :editing])
        selection    @(re-frame/subscribe [:state id :selection])
        hidden?      (partial hidden? id)
        select-cell  (fn [cell-ref event] (re-frame/dispatch [:set-first-selection id cell-ref]))
        edit-cell    (fn [cell-ref event] (re-frame/dispatch [:set-editing id cell-ref]))
        --on-blur    (fn [cell-ref f] (fn [event]
                                        (do
                                          (re-frame/dispatch [:set-editing id nil])
                                          (when (ifn? f) (f event)))))
        --on-change  (fn [cell-ref f] #(do (re-frame/dispatch [:set-cell-val id cell-ref (:value (first %))])
                                           (when (ifn? f) (f %))))]
    [:tr.Body-row {:key   (util/slug id "body" "row" n)
                   :class (util/slug "row" n)
                   :style {:height (str row-height "px")}}
     (if (some? row)
       (doall
        (for [{:keys [col-ref cell-ref value display-value type editing?]} row]
          (when (not (hidden? col-ref))
            (let [editable? (if-let [m (meta value)] (:editable? m) (if (= type :map) (:editable? value) editable?))
                  params    {:key   (util/slug id "body" "cell" cell-ref)
                             :class (str/join " " [(util/slug "cell" cell-ref)
                                                   (util/slug "col" col-ref)
                                                   (name type)
                                                   (if editable? "editable" "not-editable")])}]
              [:td.Body-cell params
               (case type
                 :fn  (value {:row row :editable editable? :cell-ref cell-ref})
                 :map (if (= editing cell-ref)
                        (if-let [{:keys [items]} value]
                          (let [{:keys [on-select on-change on-blur]} value
                                value (dissoc value :on-select :on-change :on-blur)]
                            [chooser (merge {:id              (util/slug "chooser" cell-ref)
                                             :labels          false
                                             :multiple        false
                                             :deletable       true
                                             :close-on-select true
                                             :predicate?      util/case-insensitive-includes?
                                             :selected        (filter #(= (:value %) display-value) items)
                                             :on-blur         (--on-blur cell-ref (if (ifn? on-blur) (partial on-blur row cell-ref) nil))
                                             :on-select       (--on-change cell-ref (if (ifn? on-select) (partial on-select row cell-ref) nil))
                                             :on-change       #(when (ifn? on-change) (on-change %))
                                             :auto-focus      true} value)])
                          (let [{:keys [on-change]} value]
                            [textfield {:placeholder display-value
                                        :on-change   (--on-change cell-ref (partial on-change row))}]))
                        [:span.Has-chooser {:on-click (partial edit-cell cell-ref)}
                         ;; :on-click        (partial select-cell cell-ref)
                         display-value])
                 [:span display-value])]))))
       (doall
        (for [x (range column-count)]
          [:td.Body-cell {:key (util/slug id "body" n "empty-cell" x)
                          :class (str/join [(util/slug "cell" "temporary") "can-edit"])} ""])))]))

(defn- table-body [id]
  (let [row-count @(re-frame/subscribe [:row-count id])]
    (when (> row-count 0)
     (into [:tbody.selectable] (->> (range 0 row-count)
                                    (map (fn [n] [table-body-row id n]))
                                    (doall))))))

;; Dispatches an event whenever the user scrolls the table-view.
;; The event calculates what elements needs to be rendered, ignoring
;; elements outside of the active viewport.
(defn- virtualize [id scroll-container]
  (letfn [(scroll-listener [event]
            (re-frame/dispatch-sync [:scroll-top id (.-scrollTop (.-target event))]))]
    (.addEventListener scroll-container "scroll" scroll-listener)))

(defn sheet
  "Creates a worksheet.
  To create a valid worksheet you'll need to supply a `name`-parameter
  and some `data`.

  Examples:
  TODO Write a bunch of examples
  "
  [{:keys [hidden] :as params} data]
  (let [id                (util/slug (:name params))
        ascending?        (re-frame/subscribe [:sort-ascending? id])
        table-height      (re-frame/subscribe [:table-height id])
        scroll-container* (atom nil)]
    (letfn [(view []
              (when-not hidden
                [:div.Worksheet.fill {:key id}
                 [:div.Table
                  [:div.Table-Header
                   [table id {} [table-header id]]]
                  [:div.Table-Body {:ref #(reset! scroll-container* %)}
                   [table id {:style {:height @table-height}}
                    [table-body id]]]]]))]
      #?(:clj view
         :cljs (reagent/create-class
                {:display-name "sheet"
                 :component-will-mount #(re-frame/dispatch [:sheet id data params])
                 :component-did-mount #(when-let [scroll-container @scroll-container*]
                                         (virtualize id scroll-container))
                 :reagent-render view})))))
