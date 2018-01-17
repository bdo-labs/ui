(ns ui.element.numbers.views
  (:require [#?(:clj clojure.core
                :cljs reagent.core) :refer [atom]]
            [clojure.set :as set]
            [re-frame.core :refer [subscribe dispatch]]
            [ui.util :as u]
            [ui.wire.polyglot :refer [translate]]
            [ui.element.numbers.events]
            [ui.element.numbers.subs]
            [ui.element.button :refer [button]]
            [ui.element.menu :as menu]
            [ui.element.containers :refer [container]]
            [ui.element.textfield :refer [textfield]]
            [ui.element.checkbox :refer [checkbox]]
            [ui.element.content :refer [hr]]
            [ui.element.clamp :refer [clamp]]
            [clojure.string :as str]
            [ui.util :as util]))


(def element-value #(.-value (.-target %)))


;; TODO Range-filter for numbers
;; TODO Date-filtering
;; TODO Make a permanent footer (Removes the little border issue at the bottom)
;; TODO Convert to title-row, add/remove row, hide row
;; TODO Replace spacer with a drop-down


(defn table
  [& content]
  (into [:table {:cell-padding 0, :cell-spacing 0}] content))


(defn colgroup
  ""
  [id columns column-widths hide-columns]
  (let [pad (- (count columns) (or (count column-widths) 0))
        column-widths (-> (mapv #(if (int? %) (+ % 20) :auto) column-widths)
                          (into (vec (take pad (repeat :auto)))))]
    (into
      [:colgroup]
      (->>
        columns
        (map-indexed
          (fn [n column]
            (when-not (contains? hide-columns (:col-ref column))
              (let [width (nth column-widths n)
                    ref (if-not (nil? (:col-ref column)) (:col-ref column) n)]
                [:col {:key (str id "-" ref), :width width}]))))))))


(defn string-filter
  "Outputs a list of unique values of a column"
  [sheet-ref col-ref]
  (let [;; filter-eq (fn [id value] #(dispatch [:filter-eq sheet-ref col-ref value id]))
        ;; filter-smart-case #(dispatch [:filter-smart-case sheet-ref col-ref
        ;; (element-value %)])
        ;; query             @(subscribe [:query sheet-ref col-ref])
        ;; unique @(subscribe [:unique-values sheet-ref col-ref])
        ]
    [:div]
    #_[:div
     [textfield {:label     "Search"
                 :value     query
                 :on-change filter-smart-case}]
     [:br]
     (doall (for [n (range (count unique))]
              (let [value (nth unique n)
                    id (util/slug "filter" col-ref n)
                    checked @(subscribe [:checkbox sheet-ref col-ref id])]
                [checkbox
                 {:id id,
                  :key (util/slug "key" id),
                  :checked checked,
                  :value value,
                  :on-change (filter-eq id value)} value])))]))


(defn date-filter
  "Output a date-picker"
  [sheet-ref col-ref]
  [:span])


(defn number-filter
  "Output a range slider with multiple knobs"
  [sheet-ref col-ref]
  [:span]
  #_(let [values @(subscribe [:unique-values sheet-ref col-ref])
        on-change #(dispatch [:filter-range sheet-ref col-ref %])]
    [clamp
     {:id (util/slug "filter" col-ref "range"),
      :labels? true,
      :on-change on-change,
      :range :both} values]))


(defn column-menu
  "A dropd-down menu for each column of a worksheet for filtering and
  sorting the results"
  [sheet-ref col-ref]
  (let [column @(subscribe [:column sheet-ref col-ref])
        show-column-menu? @(subscribe [:show-column-menu? sheet-ref col-ref])
        sort-ascending #(dispatch [:sort-column sheet-ref col-ref true])
        sort-descending #(dispatch [:sort-column sheet-ref col-ref false])]
    [menu/dropdown {:open? show-column-menu?}
     [container {:layout :vertically, :gap? false, :fill? true}
      [button
       {:key (str col-ref "-sort-asc"),
        :fill true,
        :class "secondary",
        :on-click sort-ascending} (translate :sort-ascending)]
      [button
       {:key (str col-ref "-sort-dsc"),
        :fill true,
        :class "secondary",
        :on-click sort-descending} (translate :sort-descending)]
      [container
       {:layout :vertically,
        :wrap? false,
        :fill? true,
        :gap? false,
        :style {:max-height "25em", :overflow :auto}}
       (case (:type column)
         :number [number-filter sheet-ref col-ref]
         :inst [date-filter sheet-ref col-ref]
         [string-filter sheet-ref col-ref])]]]))


(defn headings
  "Headings include column-headings, caption and all of the title-rows"
  [sheet-ref
   {:keys [caption? row-heading column-heading hide-columns column-count],
    :or {column-heading :hidden, row-heading :hidden, hide-columns #{}}}]
  (let [col-refs @(subscribe [:col-refs sheet-ref])
        title-rows @(subscribe [:title-rows sheet-ref])
        sort-ascending? @(subscribe [:sort-ascending? sheet-ref] {})
        sorted-column @(subscribe [:sorted-column sheet-ref])
        toggle-column-menu (fn [col-ref]
                             #(dispatch [:show-column-menu sheet-ref col-ref]))]
    [:thead ;; Column headings
     (when-not (= column-heading :hidden)
       [:tr.Column-headings {:key "Column-headings"}
        (if (= row-heading :select)
          [:th.Select {:key "Column-select"} [:input {:type :checkbox}]]
          (when-not (= row-heading :hidden)
            [:th {:key "Column-filler", :style {:border 0}}]))
        (when-not (= row-heading :hidden)
          [:th {:style {:border-top 0, :border-bottom 0}}])
        (->> col-refs
             (map #(when-not (contains? hide-columns (util/col-ref %))
                    [:th.Column-heading
                     {:key (str "Column-" (name column-heading) %),
                      :class (name column-heading)}
                     (case column-heading
                       :numeric (util/col-num %)
                       :alpha %)
                     [:button.Dropdown-origin {:on-click (toggle-column-menu %)}
                      "›"] [column-menu sheet-ref %]])))]) ;; Caption
     ;; REVIEW Are there any valid reasons why we should use a real
     ;; caption-element?
     (when (or caption? (not= column-heading :hidden))
       [:tr {:key "Column-Spacer"}
        (when (and (not= column-heading :hidden) (not= row-heading :hidden))
          [:th {:style {:border-left 0, :border-right 0}}])
        (when (and (not= column-heading :hidden) (not= row-heading :hidden))
          [:th {:style {:border-left 0, :border-right 0, :border-bottom 0}}])
        [:th
         {:col-span (- column-count (count hide-columns)),
          :style {:border-left 0, :border-right 0}}
         (when caption? [:h2.Caption (str sheet-ref)])]])
     (when-not (empty? title-rows)
       ;; Title-rows
       (->>
         title-rows
         (map
           (fn [row]
             (let [row-num (u/row-num (:cell-ref (first row)))]
               [:tr.Titlerow {:key (str "Titlerow-" row-num)}
                (when-not (= row-heading :hidden)
                  ;; TODO Move to stylesheet
                  [:th.numeric
                   {:style (if (= column-heading :hidden)
                             {:border-top "1px solid rgb(230,230,230)"}
                             {})} row-num])
                (when-not (= row-heading :hidden)
                  [:th {:style {:border-top 0, :border-bottom 0}}])
                (map-indexed
                  (fn [n title]
                    (let [n (inc n)]
                      (when-not (contains? hide-columns
                                           (util/col-ref (:cell-ref title)))
                        [:th.Titlecolumn {:key (str "Title-" n)}
                         [:span (:value title)]
                         (when (= sorted-column (u/col-ref (:cell-ref title)))
                           [:span.Arrow (if sort-ascending? "↑" "↓")])
                         (when (and (= column-heading :hidden) (= row-num 1))
                           [:span
                            [:button.Dropdown-origin
                             {:on-click (toggle-column-menu
                                          (u/col-ref (:cell-ref title)))} "›"]
                            [column-menu sheet-ref
                             (u/col-ref (:cell-ref title))]])])))
                  row)])))))]))



(defn body-row
  [sheet-ref
   {:keys [editable? row-heading number-formatter inst-formatter on-double-click
           hide-columns lock-columns],
    :or {row-heading :hidden,
         number-formatter u/format-number-en,
         inst-formatter u/format-inst,
         hide-columns #{},
         lock-columns #{}}} row-n]
  (let [row @(subscribe [:row sheet-ref row-n])
        row-num (u/row-num (:cell-ref (first row)))
        on-double-click-local (fn [m] #(on-double-click (merge m {:event %})))]
    [:tr ;; Row-heading
     (when-not (= row-heading :hidden)
       (case row-heading
         :numeric [:td.numeric {:class (if (>= row-num 100) "smaller" "")}
                   row-num]
         :select [:td.select [:input {:type :checkbox}]]
         [:td])) ;; Add space between row-heading and body-content
     (when-not (= row-heading :hidden)
       [:td.spacer {:style {:border-top 0, :border-bottom 0}}]) ;; Rows
     (->>
       row
       (map-indexed
         (fn [x {:keys [cell-ref value align fill], :as c}]
           (when-not (contains? hide-columns (util/col-ref cell-ref))
             (let [editable? (if (and (meta value)
                                      (not (nil? (:editable? (meta value)))))
                               (:editable? (meta value))
                               editable?)
                   k (merge {:key cell-ref,
                             :style (merge {}
                                           (when ((complement nil?) align)
                                             {:text-align align})
                                           (when ((complement nil?) fill)
                                             {:background fill})),
                             :on-double-click
                               (on-double-click-local
                                 {:editable (and editable?
                                                 (not (contains? lock-columns
                                                                 (util/col-ref
                                                                   cell-ref)))),
                                  :cell-ref cell-ref}),
                             :class (u/names->str
                                      [(if editable? :editable :not-editable)
                                       #_(when (and (> (count lock-columns) x)
                                                    (nth lock-columns x))
                                           :Lock-Columns)])}
                            (when editable? {:title cell-ref}))]
               (cond (number? value) [:td.cell.number k
                                      [:span (number-formatter value)]]
                     (inst? value) [:td.cell.date k
                                    [:span (inst-formatter value)]]
                     (vector? value) [:td.cell.custom k value]
                     (fn? value)
                       [:td.cell.custom k
                        (value {:row row,
                                :editable (and editable?
                                               (not (contains? lock-columns
                                                               (util/col-ref
                                                                 cell-ref)))),
                                :cell-ref cell-ref})]
                     :else [:td.cell.string k
                            (when-not (nil? value)
                              (if (str/starts-with? value "http")
                                [:a {:href value} value]
                                [:span value]))])))))
       (doall))]))


(defn body
  [sheet-ref
   {:keys [editable? row-heading empty-message hide-columns],
    :or {empty-message " "},
    :as params}]
  (let [sort-asc @(subscribe [:sort-ascending?]) ;; This is de-referenced only
                                                 ;; to make the view re-render
        row-count @(subscribe [:row-count sheet-ref])]
    (if (> 0 row-count)
      [:tbody.empty [:tr [:td empty-message]]]
      [:tbody
       (for [n (range 0 (dec row-count))]
         ^{:key (str "Row:" n)}
         [body-row sheet-ref
          (select-keys params
                       [:editable? :row-heading :number-formatter
                        :inst-formatter :on-double-click :hide-columns
                        :lock-columns]) n])])))



(defn sheet
  [params data]
  (let [sheet-ref (:name params)
        row-count (subscribe [:row-count sheet-ref])
        columns (subscribe [:columns sheet-ref])]
    (dispatch [:sheet sheet-ref data])
    (fn
      [{:keys [editable? caption? hidden hide-columns column-widths
               row-heading],
        :or {editable? false,
             row-heading :hidden,
             column-widths [],
             hide-columns #{}},
        :as params} data]
      (when (and (not hidden) (some? @row-count))
        (let [classes (u/names->str (into [(when editable? :editable)
                                           (when caption? :caption)]
                                          (:class params)))]
          [:div.Worksheet.fill {:key sheet-ref, :class classes}
           [:div.Table
            [:div.Headers
             [table
              [colgroup "headers"
               (if (= row-heading :hidden) @columns (into [nil nil] @columns))
               (if (= row-heading :hidden)
                 column-widths
                 (into [20 0] column-widths)) hide-columns]
              [headings sheet-ref
               (assoc params :column-count (count @columns))]]]
            [:div.Body
             [table
              [colgroup "headers"
               (if (= row-heading :hidden) @columns (into [nil nil] @columns))
               (if (= row-heading :hidden)
                 column-widths
                 (into [20 0] column-widths)) hide-columns]
              [body sheet-ref params]]]]])))))
