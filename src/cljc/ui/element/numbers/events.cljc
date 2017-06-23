(ns ui.element.numbers.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx]]
            #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [ui.util :as u]
            [clojure.string :as str]))



(spec/def ::name (spec/and string? not-empty))
(spec/def ::title (spec/and string? #(not (str/starts-with? % "http"))))
(spec/def ::titles (spec/coll-of ::title))
(spec/def ::column-heading #{:alpha :numeric :hidden})
(spec/def ::row-heading #{:alpha :numeric :select :hidden})
(spec/def ::type #{:number :inst :string})

(spec/def ::lock (spec/coll-of boolean?))

(spec/def ::col-ref (spec/with-gen
                      (spec/and keyword? #(re-matches #"[A-Z]+" (name %)))
                      #(spec/gen #{:A :ZA :ABA :ACMK :FOO})))


(spec/def ::cell-ref (spec/with-gen
                      (spec/and keyword? #(re-matches #"[A-Z]+[0-9]+" (str (name %))))
                      #(spec/gen #{:A1 :Z999 :AZ3 :BOBBY6 :ME2})))


#_(spec/def ::rows (spec/map-of ::cell-ref any?))
(spec/def ::rows (spec/coll-of map?))


(spec/def ::scale (spec/or :inst inst? :number number?))
(spec/def ::min ::scale)
(spec/def ::max ::scale)


(spec/def ::caption? boolean?)
(spec/def ::editable? boolean?)
(spec/def ::sortable? boolean?)
(spec/def ::filterable? boolean?)
(spec/def ::freeze? boolean?)


(spec/def ::inst-formatter fn?)
(spec/def ::number-formatter fn?)


(spec/def ::csv
  (spec/and (spec/coll-of vector?)
            (spec/cat :title-rows (spec/* ::titles)
                      :rows (spec/* any?))))


(spec/def ::csv-no-title
  (spec/and (spec/coll-of vector?)
            (spec/cat :rows (spec/* any?))))


(spec/def ::unq-column (spec/map-of #{:rows} any?))
(spec/def ::unq-columns (spec/coll-of ::unq-column))


(spec/def ::data
  (spec/or
   :data         ::unq-columns
   :csv          ::csv
   :csv-no-title ::csv-no-title))


(spec/def ::column
  (spec/keys :req-un [::type ::col-ref ::rows]
             :opt-un [::min ::max ::freeze? ::editable? ::sortable? ::filterable? ::lock]))


(spec/def ::columns
  (spec/coll-of ::column))


(spec/def ::params
  (spec/keys :req-un [::name]
             :opt-un [::column-heading
                      ::row-heading
                      ::editable?
                      ::caption?]))


(spec/def ::args (spec/cat :params ::params :content ::data))


;;-----

(defn cell-ref
  "Generate a cell-ref from [col-ref] and [index]"
  [col-ref index]
  (keyword (str (name col-ref) index)))

(spec/fdef cell-ref
           :args (spec/cat :col-ref :col-ref :num nat-int?)
           :ret keyword?)


;; Each column will need to be typed in order to perform
;; sorting and filtering. The type-inference is based on the
;; first, non title-row value, so if you try to mix and match
;; values, you will probably encounter breakage.
(defn infer-column [col]
  (let [rows      (->> (:rows col)
                       (remove :title-row?)
                       (map :value))
        first-val (first rows)]
    (merge col (cond
                 (number? first-val) {:type :number
                                      :min  (reduce min rows)
                                      :max  (reduce max rows)}
                 (inst? first-val)   {:type :inst
                                      :min  (reduce < rows)
                                      :max  (reduce > rows)}
                 (string? first-val) {:type :string}
                 :else               {:type :any}))))


(defn data->columns
  "Transform the [data] passed in; to manageable columns"
  [data]
  (->> (apply map list data)
       (map-indexed
        (fn [n col]
          (let [col-ref (u/col-refs n)
                col     (vec (map-indexed #(merge {:cell-ref (cell-ref col-ref (inc %1))} %2) col))]
            (infer-column {:col-ref col-ref
                           :filters {}
                           :rows    col}))))
       (vec)))


(defn csv-no-title->columns
  [{:keys [rows]}]
  (->> rows
       (mapv #(mapv (fn [col] {:value col :title-row? false}) %))))


(defn csv->columns [{:keys [title-rows rows] :as data}]
  (let [title-rows (->> title-rows (mapv #(mapv (fn [col] {:value col :title-row? true}) %)))
        rows (csv-no-title->columns data)]
    (into title-rows rows)))


(defn ->columns [[k v]]
  (data->columns
   (case k
     :csv          (csv->columns v)
     :csv-no-title (csv-no-title->columns v)
     v)))


;; Handlers
(defn initialize-sheet
  [{:keys [db]} [_ sheet-ref d]]
  (if-let [data (spec/conform ::data d)]
    (let [columns (->columns data)]
      {:db (assoc db sheet-ref {:columns      columns
                                :initialized? true})})
    {:db (assoc db sheet-ref {:columns      []
                              :initialized? false})}))


(reg-event-fx :initialize-sheet initialize-sheet)


(reg-event-db :sort-ascending?
              (fn handle-sort-ascending? [db [_ sheet-ref]]
                (update-in db [sheet-ref :sort-ascending?] not)))


(reg-event-db :sort-column
              (fn handle-sort-column [db [_ sheet-ref col-ref ascending?]]
                (update-in db [sheet-ref] assoc
                           :sorted-column col-ref
                           :sort-ascending? ascending?)))


(reg-event-db :add-filter
              (fn handle-add-filter [db [_ sheet-ref col-ref filter-name filter-fn]]
                (update-in db [sheet-ref :columns (u/col-num col-ref) :filters] assoc filter-name filter-fn)))


(reg-event-db :remove-filter
              (fn handle-remove-filter [db [_ sheet-ref col-ref filter-name]]
                (update-in db [sheet-ref :columns (u/col-num col-ref) :filters] dissoc filter-name)))


(reg-event-fx :toggle-filter
              (fn handle-toggle-filter [{:keys [db]} [_ sheet-ref col-ref filter-name filter-fn]]
                (let [filter-exists?  ^boolean (not (nil? (get-in db [sheet-ref :columns (u/col-num col-ref) :filters filter-name] nil)))]
                  (if filter-exists?
                    {:dispatch [:remove-filter sheet-ref col-ref filter-name]}
                    {:dispatch [:add-filter sheet-ref col-ref filter-name filter-fn]}))))


(reg-event-fx :filter-eq
              (fn handle-filter-eq [{:keys [db]} [k sheet-ref col-ref s]]
                (let [k (keyword (u/slug (name k) " " s))
                      f (partial = s)]
                  {:dispatch [:toggle-filter sheet-ref col-ref k f]})))


(reg-event-fx :filter-range
              (fn handle-filter-range [{:keys [db]} [k sheet-ref col-ref v]]
                (let [f #(and (<= (:min v) %)
                              (>= (:max v) %))]
                  {:dispatch [:toggle-filter sheet-ref col-ref k f]})))


(reg-event-fx :set-filter-min
              (fn [{:keys [db]} [_ sheet-ref col-ref val]]
                {:db (update-in db [sheet-ref :columns (u/col-num col-ref) :filter-min] val)}))


(reg-event-fx :set-filter-max
              (fn [{:keys [db]} [_ sheet-ref col-ref val]]
                {:db (update-in db [sheet-ref :columns (u/col-num col-ref) :filter-max] val)}))


(reg-event-fx :toggle-filter-term
              (fn [{:keys [db]} [_ sheet-ref col-ref val]]
                {:db (update-in db [sheet-ref :columns (u/col-num col-ref) :filter-terms]
                                #(if (contains? % val)
                                   (disj % val)
                                   (conj % val)))}))




(reg-event-db :show-column-menu
              (fn handle-show-column-menu [db [_ sheet-ref col-ref]]
                (update-in db [sheet-ref :show-column-menu]
                           #(if-not (= col-ref %) col-ref nil))))


(reg-event-db :hide-column-menu
              (fn handle-hide-column-menu [db [_ sheet-ref]]
                (update-in db [sheet-ref] dissoc :show-column-menu)))


(reg-event-db :unselect
              (fn handle-unselect [db [_ sheet-ref]]
                (update-in db [sheet-ref] dissoc :selection)))


(reg-event-db :set-first-selection
              (fn handle-set-selection [db [_ sheet-ref cell-ref]]
                (assoc-in db [sheet-ref :selection] {:column (u/col-num cell-ref)
                                                    :rows (u/row-num cell-ref)})))


(reg-event-db :add-to-selection
              (fn handle-set-selection [db [_ sheet-ref cell-ref]]
                (update-in db [sheet-ref :selection :rows] #(range (first %) (u/row-num cell-ref)))))


(reg-event-db :set-cell-val
              (fn handle-set-cell-val [db [_ sheet-ref cell-ref value]]
                (assoc-in db [sheet-ref
                              :columns (u/col-num cell-ref)
                              :rows (u/row-num cell-ref)
                              :value] value)))
