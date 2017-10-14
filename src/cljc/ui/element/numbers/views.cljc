(ns ui.element.numbers.views
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.set :as set]
            [re-frame.core :refer [subscribe dispatch]]
            [ui.util :as u]
            [ui.element.numbers.events]
            [ui.element.numbers.subs]
            [ui.element.button :refer [button]]
            [ui.element.menu :as menu]
            [ui.element.containers :refer [container]]
            [ui.element.clamp :refer [clamp]]
            [clojure.string :as str]
            [ui.util :as util]))


;; TODO Range-filter for numbers
;; TODO Date-filtering
;; TODO Make a permanent footer (Removes the little border issue at the bottom)
;; TODO Convert to title-row, add/remove row, hide row
;; TODO Replace spacer with a drop-down


(defn table [& content]
  (into [:table {:cell-padding 0
                 :cell-spacing 0}] content))


(defn colgroup
  ""
  [id columns column-widths hide-columns]
  (let [pad           (- (count columns)
                         (or (count column-widths) 0))
        column-widths (-> (mapv #(if (int? %) (+ % 20) :auto) column-widths)
                          (into (vec (take pad (repeat :auto)))))]
    (into [:colgroup]
          (->> columns
               (map-indexed (fn [n column]
                              (when-not (contains? hide-columns (:col-ref column))
                                (let [width (nth column-widths n)
                                      ref   (if-not (nil? (:col-ref column)) (:col-ref column) n)]
                                  [:col {:key   (str id "-" ref)
                                         :width width}]))))))))


(def extract-value #(.-value (.-target %)))


(defn string-filter
  "Outputs a list of unique values of a column"
  [sheet-ref col-ref]
  (let [filter-eq #(dispatch [:filter-eq sheet-ref col-ref (extract-value %)])
        unique    @(subscribe [:unique-values sheet-ref col-ref])]
    (->> unique
         (map-indexed
          (fn [n s]
            [:div.Row {:key (str "uniq-" col-ref "-" n)}
             [:input {:id        (str "filter-" col-ref "-" n)
                      :key       (str "uniq-input-" col-ref "-" n)
                      :type      :checkbox
                      :value     s
                      :on-change filter-eq}]
             [:label {:key (str "uniq-label-" col-ref "-" n)
                      :for (str "filter-" col-ref "-" n)} s]])))))


(defn date-filter
  [sheet-ref col-ref]
  (let []
    [:span "date"]))


(defn number-filter
  "Output a range slider with multiple knobs"
  [sheet-ref col-ref]
  (let [values    @(subscribe [:unique-values sheet-ref col-ref])
        on-change #(dispatch [:filter-range sheet-ref col-ref %])]
    [clamp {:id        (str "filter-" col-ref "-range")
            :labels?   true
            :on-change on-change
            :range     :both} values]))


(defn column-menu
  "A dropd-down menu for each column of a worksheet for filtering and
  sorting the results"
  [sheet-ref col-ref]
  (let [column            @(subscribe [:column sheet-ref col-ref])
        show-column-menu? @(subscribe [:show-column-menu? sheet-ref col-ref])
        sort-ascending    #(dispatch [:sort-column sheet-ref col-ref true])
        sort-descending   #(dispatch [:sort-column sheet-ref col-ref false])]
    [menu/dropdown {:open? show-column-menu?}
     [container {:layout :vertically
                 :gap?   false
                 :fill?  true}
      [button {:key      (str col-ref "-sort-asc")
               :fill?    true
               :on-click sort-ascending} "Sort Ascending"]
      [button {:key      (str col-ref "-sort-dsc")
               :fill?    true
               :on-click sort-descending} "Sort Descending"]
      [container {:layout :vertically
                  :wrap? false
                  :fill?  true
                  :gap?   false
                  :style  {:max-height "25em"
                           :overflow   :auto}}
       (case (:type column)
         :number (number-filter sheet-ref col-ref)
         :inst   (date-filter sheet-ref col-ref)
         (string-filter sheet-ref col-ref))]]]))


(defn headings
  "Headings include column-headings, caption and all of the title-rows"
  [sheet-ref {:keys [caption? row-heading column-heading column-widths hide-columns]
              :or   {column-heading :hidden
                     row-heading    :hidden
                     hide-columns   #{}}}]
  (let [col-refs           @(subscribe [:col-refs sheet-ref])
        columns            @(subscribe [:columns sheet-ref])
        title-rows         @(subscribe [:title-rows sheet-ref])
        sort-ascending?    @(subscribe [:sort-ascending? sheet-ref] {})
        sorted-column      @(subscribe [:sorted-column sheet-ref])
        toggle-column-menu (fn [col-ref] #(dispatch [:show-column-menu sheet-ref col-ref]))]
    [:div.Headers
     [table
      [colgroup "headers"
       (if (= row-heading :hidden) columns (into [nil nil] columns))
       (if (= row-heading :hidden) column-widths (into [20 0] column-widths ))
       hide-columns]
      [:thead
       ;; Column headings
       (when-not (= column-heading :hidden)
         [:tr.Column-headings {:key "Column-headings"}
          (if (= row-heading :select)
            [:th.Select {:key "Column-select"} [:input {:type :checkbox}]]
            (when-not (= row-heading :hidden) [:th {:key "Column-filler" :style {:border 0}}]))
          (when-not (= row-heading :hidden)
            [:th {:style {:border-top 0 :border-bottom 0}}])
          (->> col-refs
               (map #(when-not (contains? hide-columns (util/col-ref %))
                       [:th.Column-heading
                        {:key   (str "Column-" (name column-heading) %)
                         :class (name column-heading)}
                        (case column-heading
                          :numeric (util/col-num %)
                          :alpha   %)
                        [:button.Dropdown-origin {:on-click (toggle-column-menu %)} "›"]
                        [column-menu sheet-ref %]])))])
       ;; Caption
       ;; REVIEW Are there any valid reasons why we should use a real caption-element?
       (when (or caption?
                 (not= column-heading :hidden))
         [:tr {:key "Column-Spacer"}
          (when (and (not= column-heading :hidden)
                     (not= row-heading :hidden))
            [:th {:style {:border-left 0 :border-right 0}}])
          (when (and (not= column-heading :hidden)
                     (not= row-heading :hidden))
            [:th {:style {:border-left 0 :border-right 0 :border-bottom 0}}])
          [:th {:col-span (- (count columns) (count hide-columns)) :style {:border-left 0 :border-right 0}}
           (when caption?
             [:h2.Caption (str sheet-ref)])]])
       ;; Title-rows
       (->> title-rows
            (map (fn [row]
                   (let [row-num (u/row-num (:cell-ref (first row)))]
                     [:tr.Titlerow {:key (str "Titlerow-" row-num)}
                      (when-not (= row-heading :hidden)
                        ;; TODO Move to stylesheet
                        [:th.Index {:style (if (= column-heading :hidden)
                                             {:border-top "1px solid rgb(230,230,230)"}
                                             {})} row-num])
                      (when-not (= row-heading :hidden)
                        [:th {:style {:border-top 0 :border-bottom 0}}])
                      (map-indexed (fn [n title]
                                     (when-not (contains? hide-columns (util/col-ref (:cell-ref title)))
                                       [:th.Titlecolumn {:key (str "Title-" n)}
                                        [:span (:value title)]
                                        (when (= sorted-column (u/col-ref (:cell-ref title)))
                                          [:span.Arrow
                                           (if sort-ascending? "↑" "↓")])
                                        (when (and (= column-heading :hidden)
                                                   (= row-num 0))
                                          [:span
                                           [:button.Dropdown-origin {:on-click (toggle-column-menu (u/col-ref (:cell-ref title)))} "›"]
                                           [column-menu sheet-ref (u/col-ref (:cell-ref title))]
                                           ])])) row)]))))]]]))


(defn body
  [sheet-ref {:keys [editable? row-heading column-widths number-formatter inst-formatter on-double-click empty-message hide-columns lock-columns]
              :or   {column-widths    []
                     row-heading      :hidden
                     number-formatter u/format-number-en
                     inst-formatter   u/format-inst
                     empty-message    " "
                     hide-columns #{}
                     lock-columns #{}}}]
  (let [columns               @(subscribe [:columns sheet-ref])
        rows                  @(subscribe [:rows sheet-ref])
        on-double-click-local (fn [m] #(on-double-click (merge m {:event %})))]
    [:div.Body
     [table
      [colgroup "headers"
       (if (= row-heading :hidden) columns (into [nil nil] columns))
       (if (= row-heading :hidden) column-widths (into [20 0] column-widths ))
       hide-columns]
      (if (empty? rows)
        [:tbody.empty [:tr [:td empty-message]]]
        (into [:tbody]
              (->> rows
                   (map-indexed (fn [n row]
                                  (let [row-index (u/row-num (:cell-ref (first row)))]
                                    (into [:tr {:key (str "Row:" row-index)}
                                           ;; Row-heading
                                           (when-not (= row-heading :hidden)
                                             (case row-heading
                                               :numeric [:td.index {:class (if (>= (inc n) 100) "smaller" "")} (inc n)]
                                               :select  [:td.select [:input {:type :checkbox}]]
                                               [:td]))
                                           ;; Add space between row-heading and body-content
                                           (when-not (= row-heading :hidden)
                                             [:td.spacer {:style {:border-top 0 :border-bottom 0}}])]
                                          ;; Rows
                                          (->> row
                                               (map-indexed (fn [x {:keys [cell-ref value align fill] :as c}]
                                                              (when-not (contains? hide-columns (util/col-ref cell-ref))
                                                                (let [k (merge
                                                                         {:key             cell-ref
                                                                          :style           (merge {}
                                                                                                  (when ((complement nil?) align) {:text-align align})
                                                                                                  (when ((complement nil?) fill) {:background fill}))
                                                                          :on-double-click (on-double-click-local {:editable (and editable? (not (contains? lock-columns (util/col-ref cell-ref))))
                                                                                                                   :cell-ref cell-ref})
                                                                          :class           (u/names->str [(when editable? :Editable)
                                                                                                          #_(when (and (> (count lock-columns) x)
                                                                                                                     (nth lock-columns x)) :Lock-Columns)])}
                                                                         (when editable? {:title cell-ref}))]
                                                                  (cond
                                                                    (number? value) [:td.cell.number k [:span (number-formatter value)]]
                                                                    (inst? value)   [:td.cell.date k [:span (inst-formatter value)]]
                                                                    (vector? value) [:td.cell.custom k value]
                                                                    (fn? value)     [:td.cell.custom k (value {:row      row
                                                                                                               :editable (and editable? (not (contains? lock-columns (util/col-ref cell-ref))))
                                                                                                               :cell-ref cell-ref})]
                                                                    :else           [:td.cell.string k
                                                                                     (when-not (nil? value)
                                                                                       (if (str/starts-with? value "http")
                                                                                         [:a {:href value} value]
                                                                                         [:span value]))])))))))))))))]]))


(defn sheet
  [params data]
  (let [sheet-ref  (:name params)
        title-rows (subscribe [:title-rows sheet-ref])]
    (dispatch [:sheet sheet-ref data])
    (fn [{:keys [editable? caption?]
         :or   {editable? false}
         :as   params} data]
      (when-not (empty? @title-rows)
        [:div.Worksheet.fill {:class (u/names->str (into [(when editable? :editable)
                                                          (when caption? :caption)] (:class params)))}
         [:div.Table
          [headings sheet-ref params]
          [body sheet-ref params]]]))))

