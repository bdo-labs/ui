(ns ui.element.numbers.events
  (:require [ui.element.numbers.specs :as spec]
            [re-frame.core :as re-frame]
            [ui.element.numbers.helpers :refer [->columns sheet-path]]
            [ui.util :as util]))

;; Data Manipulation ------------------------------------------------------

(re-frame/reg-event-fx
 :sheet
 (fn [{:keys [db]} [_ id d state]]
   (if-let [data (util/conform! ::spec/data d)]
     {:db (let [columns    (->columns data)
                hide       (:hide-columns state)
                rows       (->> columns (map :rows) (apply map list))
                title-rows (remove #((complement :title-row?) (first %)) rows)
                body-rows  (remove #(:title-row? (first %)) rows)
                unique-values (->> columns (mapv :rows) (mapv #(->> % (remove :title-row?) (mapv :display-value) (distinct) (sort <))) (vec))
                state      (merge {:buffer-pos  0
                                   :buffer-size 40
                                   :scroll-pos 0
                                   :row-height  60} state)
                state      (assoc state :table-height (* (count body-rows) (:row-height state)))]
            (assoc-in db (sheet-path id) {:columns    columns
                                          :unique-values unique-values
                                          :title-rows title-rows
                                          :rows       body-rows
                                          :state      state
                                          :column-count (->> columns (remove #(contains? hide (:col-ref %))) (count))
                                          :row-count  (count body-rows)}))}
     {:db (assoc-in db (sheet-path id) {:columns []})})))

(re-frame/reg-event-db
 :set-cell-val
 (fn [db [_ id cell-ref value]]
   (let [col-n (util/col-num cell-ref)
         row-n (dec (util/row-num cell-ref))]
     (-> db
         (assoc-in (sheet-path [:columns col-n :rows row-n :display-value] id) value)
         (assoc-in (sheet-path [:columns col-n :rows row-n :value :display-value] id) value)
         (assoc-in (sheet-path [:columns col-n :rows row-n :value :value] id) value)))))

(re-frame/reg-event-db
 :merge-cell
 (fn [db [_ id cell-ref new-cell]]
   (let [col-n (util/col-num cell-ref)
         row-n (dec (util/row-num cell-ref))
         path (sheet-path [:columns col-n :rows row-n] id)]
     (let [current-cell (get-in db path)
           cell         (merge current-cell new-cell)]
       (assoc-in db path cell)))))

(re-frame/reg-event-db
 :set-buffer-pos
 (fn [db [k id buffer-pos]]
   (let [row-count             (get-in db (sheet-path [:row-count] id))
         {:keys [buffer-size]} (get-in db (sheet-path [:state] id))
         clamped               (min (max 0 (int buffer-pos)) (int (- row-count buffer-size)))
         path (sheet-path [:state :buffer-pos] id)]
     (assoc-in db path clamped))))

(re-frame/reg-event-fx
 :scroll-top
 (fn [{:keys [db]} [k id top]]
   (let [state (get-in db (sheet-path [:state] id))
         {:keys [buffer-pos buffer-size row-height]} state
         f (if (and (<= (/ top row-height)
                        (- buffer-pos (* buffer-size 0.3)))
                    (< top 0)) + -)]
     {:dispatch [:set-buffer-pos id (int (f (/ top row-height)
                                            (* buffer-size 0.3)))]})))

(re-frame/reg-event-db
 :set-editing
 (fn [db [k id cell-ref]]
   (let [path (sheet-path [:state :editing] id)]
     (assoc-in db path cell-ref))))

;; Sorting ----------------------------------------------------------------

(re-frame/reg-event-db
 :sort-ascending?
 (fn [db [_ id]]
   (let [path (sheet-path [:state :sort-ascending?] id)]
     (update-in db path not))))

(re-frame/reg-event-db
 :sort-column
 (fn [db [_ id col-ref ascending?]]
   (let [path (sheet-path [:state :sort-column] id)]
     (update-in db path assoc
                :sorted-column col-ref
                :sort-ascending? ascending?))))

;; Filtering --------------------------------------------------------------

(re-frame/reg-event-db
 :add-filter
 (fn [db [_ id col-ref filter-name filter-fn]]
   (let [col-n (util/col-num col-ref)
         path (sheet-path [:columns col-n :filters] id)]
     (update-in db path assoc filter-name filter-fn))))

(re-frame/reg-event-db
 :remove-filter
 (fn [db [_ id col-ref filter-name]]
   (let [col-n (util/col-num col-ref)
         path (sheet-path [:columns col-n :filters] id)]
     (update-in db path dissoc filter-name))))

(re-frame/reg-event-fx
 :toggle-filter
 (fn [{:keys [db]}
      [_ id col-ref filter-name filter-fn]]
   (let [path (sheet-path [:columns (util/col-num col-ref)
                           :filters filter-name] id)]
     (if (some? (get-in db path nil))
       {:dispatch [:remove-filter id col-ref filter-name]}
       {:dispatch [:add-filter id col-ref filter-name filter-fn]}))))

(re-frame/reg-event-fx
 :filter-eq
 (fn [{:keys [db]} [k sheet-id col-ref value]]
   (let [k (keyword (util/slug (name k) " " value))
         f (partial = value)
         path (sheet-path [:columns (util/col-num col-ref)
                           :checkboxes value] sheet-id)]
     {:dispatch [:toggle-filter sheet-id col-ref k f]
      :db       (update-in db path not)})))

(re-frame/reg-event-fx
 :filter-smart-case
 (fn [{:keys [db]} [k id col-ref s]]
   (let [k     (keyword (util/slug (name k) " smart-case"))
         f     (partial util/smart-case-includes? s)
         col-n (util/col-num col-ref)
         path (sheet-path [:columns col-n :query] id)]
     {:dispatch [:toggle-filter id col-ref k f]
      :db       (assoc-in db path s)})))

(re-frame/reg-event-fx
 :filter-range
 (fn [{:keys [db]} [k id col-ref v]]
   (let [f #(and (<= (:min v) %) (>= (:max v) %))]
     {:dispatch [:toggle-filter id col-ref k f]})))

(re-frame/reg-event-fx
 :set-filter-min
 (fn [{:keys [db]} [_ id col-ref val]]
   {:db (update-in db
                   [id :columns (util/col-num col-ref) :filter-min]
                   val)}))

(re-frame/reg-event-fx
 :set-filter-max
 (fn [{:keys [db]} [_ id col-ref val]]
   {:db (update-in db
                   [id :columns (util/col-num col-ref) :filter-max]
                   val)}))

(re-frame/reg-event-fx
 :toggle-filter-term
 (fn [{:keys [db]} [_ id col-ref val]]
   (let [path (sheet-path [:columns (util/col-num col-ref) :filter-terms] id)]
    {:db (update-in db path #(if (contains? % val) (disj % val) (conj % val)))})))

;; Column menu ------------------------------------------------------------

(re-frame/reg-event-db
 :show-column-menu
 (fn [db [_ id col-ref]]
   (let [path (sheet-path [:state :show-column-menu] id)]
     (update-in db path
                #(if (not= col-ref %) col-ref nil)))))
