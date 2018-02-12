(ns ui.element.numbers.events
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [ui.element.numbers.specs :as spec]
            [clojure.string :as str]
            [re-frame.core :refer [reg-event-db reg-event-fx]]
            [ui.util :as util]))


;; Helper functions -------------------------------------------------------


(defn cell-ref
  "Generate a cell-ref from [col-ref] and [index]"
  [col-ref index]
  (keyword (str (name col-ref) index)))


;; Each column will need to be typed in order to perform
;; sorting and filtering. The type-inference is based on the
;; first, non title-row value, so if you try to mix and match
;; values, you will encounter breakage.
(defn infer-column
  [col]
  (let [rows      (->> (:rows col)
                     (remove :title-row?)
                     (map :value))
        first-val (first rows)]
    (if-let [meta-type (:type (meta first-val))]
      meta-type
      (merge col
             (cond (number? first-val)
                   {:type :number, :min (reduce min rows), :max (reduce max rows)}
                   (inst? first-val)
                   {:type :inst, :min (reduce < rows), :max (reduce > rows)}
                   (string? first-val) {:type :string}
                   :else               {:type :any})))))


(defn data->columns
  "Transform the [data] passed in; to manageable columns"
  [data]
  (let [res (->> (apply map list data)
               (map-indexed
                (fn [n col]
                  (let [col-ref (util/col-refs n)
                        col     (vec (map-indexed
                                      #(merge {:cell-ref (cell-ref col-ref (inc %1))
                                               :row-n (inc %1)
                                               :col-n n} %2)
                                      col))]
                    (infer-column {:col-ref col-ref, :filters {}, :rows col}))))
               (vec))]
    res))


(defn csv-no-title->columns
  [{:keys [rows]}]
  (->> rows
     (mapv #(mapv (fn [col] {:value col, :title-row? false}) %))))


(defn csv->columns
  [{:keys [title-rows rows], :as data}]
  (let [title-rows (->> title-rows
                      (mapv #(mapv (fn [col] {:value col, :title-row? true}) %)))
        rows       (csv-no-title->columns data)]
    (into title-rows rows)))


(defn ->columns
  [[k v]]
  (data->columns (case k
                   :csv (csv->columns v)
                   :csv-no-title (csv-no-title->columns v)
                   v)))


;; Event handlers ---------------------------------------------------------


(reg-event-fx
 :sheet
 (fn [{:keys [db]} [_ sheet-ref state d]]
   (if-let [data (util/conform! ::spec/data d)]
     {:db (let [columns (->columns data)]
            (assoc db sheet-ref {:columns columns
                                 :state   state}))}
     {:db (assoc db sheet-ref {:columns []
                               :state   {}})})))


;; Sorting


(reg-event-db
 :sort-ascending?
 (fn handle-sort-ascending? [db [_ sheet-ref]]
   (update-in db [sheet-ref :sort-ascending?] not)))


(reg-event-db
 :sort-column
 (fn handle-sort-column [db [_ sheet-ref col-ref ascending?]]
   (update-in db [sheet-ref] assoc
              :sorted-column col-ref
              :sort-ascending? ascending?)))


;; Filtering


(reg-event-db
  :add-filter
  (fn handle-add-filter [db [_ sheet-ref col-ref filter-name filter-fn]]
    (update-in db
               [sheet-ref :columns (util/col-num col-ref) :filters]
               assoc
               filter-name
               filter-fn)))


(reg-event-db
 :remove-filter
 (fn handle-remove-filter [db [_ sheet-ref col-ref filter-name]]
   (update-in db
              [sheet-ref :columns (util/col-num col-ref) :filters]
              dissoc
              filter-name)))


(reg-event-fx
  :toggle-filter
  (fn handle-toggle-filter [{:keys [db]}
                            [_ sheet-ref col-ref filter-name filter-fn]]
    (if (some? (get-in db [sheet-ref :columns (util/col-num col-ref) :filters filter-name] nil))
      {:dispatch [:remove-filter sheet-ref col-ref filter-name]}
      {:dispatch [:add-filter sheet-ref col-ref filter-name filter-fn]})))


(reg-event-fx
 :filter-eq
 (fn handle-filter-eq [{:keys [db]} [k sheet-ref col-ref s id]]
   (let [k (keyword (util/slug (name k) " " s))
         f (partial = s)]
     {:dispatch [:toggle-filter sheet-ref col-ref k f]
      :db       (update-in db [sheet-ref :columns (util/col-num col-ref) :checkboxes id] not)})))


(reg-event-fx
 :filter-smart-case
 (fn [{:keys [db]} [k sheet-ref col-ref s]]
   (let [k (keyword (util/slug (name k) " smart-case"))
         f (partial util/smart-case-includes? s)]
     {:dispatch [:toggle-filter sheet-ref col-ref k f]
      :db       (assoc-in db [sheet-ref :columns (util/col-num col-ref) :query] s)})))


(reg-event-fx
 :filter-range
 (fn handle-filter-range [{:keys [db]} [k sheet-ref col-ref v]]
   (let [f #(and (<= (:min v) %) (>= (:max v) %))]
     {:dispatch [:toggle-filter sheet-ref col-ref k f]})))


(reg-event-fx
  :set-filter-min
  (fn [{:keys [db]} [_ sheet-ref col-ref val]]
    {:db (update-in db
                    [sheet-ref :columns (util/col-num col-ref) :filter-min]
                    val)}))


(reg-event-fx
  :set-filter-max
  (fn [{:keys [db]} [_ sheet-ref col-ref val]]
    {:db (update-in db
                    [sheet-ref :columns (util/col-num col-ref) :filter-max]
                    val)}))


(reg-event-fx
  :toggle-filter-term
  (fn [{:keys [db]} [_ sheet-ref col-ref val]]
    {:db (update-in db
                    [sheet-ref :columns (util/col-num col-ref) :filter-terms]
                    #(if (contains? % val) (disj % val) (conj % val)))}))


(reg-event-db
 :show-column-menu
 (fn handle-show-column-menu [db [_ sheet-ref col-ref]]
   (update-in db
              [sheet-ref :show-column-menu]
              #(if-not (= col-ref %) col-ref nil))))


(reg-event-db
 :hide-column-menu
 (fn handle-hide-column-menu [db [_ sheet-ref]]
   (update-in db [sheet-ref] dissoc :show-column-menu)))


(reg-event-db
 :unselect
 (fn handle-unselect [db [_ sheet-ref]]
   (update-in db [sheet-ref] dissoc :selection)))


(reg-event-db
 :set-first-selection
 (fn handle-set-selection [db [_ sheet-ref cell-ref]]
   (assoc-in db
             [sheet-ref :selection]
             {:column (util/col-num cell-ref), :rows (util/row-num cell-ref)})))


(reg-event-db
 :add-to-selection
 (fn handle-set-selection [db [_ sheet-ref cell-ref]]
   (update-in db
              [sheet-ref :selection :rows]
              #(range (first %) (util/row-num cell-ref)))))


(reg-event-db
 :set-cell-val
 (fn handle-set-cell-val [db [_ sheet-ref cell-ref value]]
   (assoc-in db
             [sheet-ref :columns (util/col-num cell-ref) :rows
              (dec (util/row-num cell-ref)) :value]
             value)))
