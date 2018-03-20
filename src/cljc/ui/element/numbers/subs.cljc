(ns ui.element.numbers.subs
  (:require [re-frame.core :as re-frame]
            [ui.element.numbers.helpers :refer [sheet-path sheet-extract]]
            [ui.util :as util]))

(re-frame/reg-sub
 :sheet
 (fn [db [_ id]]
   (get-in db (sheet-path id nil))))

(re-frame/reg-sub
 :state
 (fn [db [k id param]]
   (get-in db (sheet-path [:state param] id) nil)))

;; Table Headings ---------------------------------------------------------


(re-frame/reg-sub :title-rows sheet-extract)

(re-frame/reg-sub
 :visible-title-rows
 (fn [[_ id]]
   [(re-frame/subscribe [:title-rows id])
    (re-frame/subscribe [:state id :hide-columns])])
 (fn [[title-rows hide]]
   (->> title-rows (map (fn [row] (remove #(contains? hide (:col-ref %)) row))))))

(re-frame/reg-sub
 :show-column-menu?
 (fn [[_ id]]
   (re-frame/subscribe [:state id :show-column-menu]))
 (fn [column-menu [_ _ col-ref]]
   (= col-ref column-menu)))

;; Table Columns ----------------------------------------------------------


(re-frame/reg-sub :columns sheet-extract)

(re-frame/reg-sub
 :column
 (fn [db [_ id col-ref]]
   (let [column-path (sheet-path [:columns (util/col-num col-ref)] id)]
     (get-in db column-path))))

(re-frame/reg-sub
 :column-count
 (fn [db [_ id]]
   (get-in db (sheet-path [:column-count] id))))

(re-frame/reg-sub
 :visible-columns
 (fn [[_ id]]
   [(re-frame/subscribe [:columns id])
    (re-frame/subscribe [:state id :hide-columns])])
 (fn [[columns hide]]
   (->> columns (remove #(contains? hide (:col-ref %))))))

(re-frame/reg-sub
 :col-refs
 (fn [[_ id]]
   (re-frame/subscribe [:columns id]))
 (fn [columns _]
   (map :col-ref columns)))

(re-frame/reg-sub
 :column-widths
 (fn [[_ id]]
   [(re-frame/subscribe [:visible-columns id])
    (re-frame/subscribe [:state id :column-widths])])
 (fn [[visible-columns widths]]
   (let [pad           (- (count visible-columns) (or (count widths) 0))
         column-widths (-> (mapv #(if (int? %) (+ % 20) :auto) widths)
                           (into (vec (take pad (repeat :auto)))))]
     column-widths)))

;; Table Rows -------------------------------------------------------------


(defn filter-rows [columns _]
  (when (seq columns)
    (let [filter-fns (map :filters columns)
          apply-sort-filter (fn [col-num col]
                              (if-let [filters (vals (nth filter-fns col-num))]
                                ((complement not-any?) true?
                                                       (map (fn [filter-fn]
                                                              (let [value (:value col)]
                                                                (if-not (nil? (meta value))
                                                                  (filter-fn (:sort-value (meta value)))
                                                                  (filter-fn value)))) filters)) true))]
      (->> columns
           (map :rows)
           (apply map list)
           (remove #(:title-row? (first %)))
           (filter #(not-any? false? (map-indexed apply-sort-filter %)))))))

(re-frame/reg-sub
 :rows
 (fn [[_ id]]
   (re-frame/subscribe [:columns id]))
 (fn [columns _]
   (->> columns
        (map :rows)
        (apply map list)
        (remove #(:title-row? (first %))))))

(re-frame/reg-sub
 :filters
 (fn [[_ id]]
   (re-frame/subscribe [:columns id]))
 (fn [columns [_ id]]
   (map :filters columns)))

(re-frame/reg-sub
 :filtered-rows
 (fn [[_ id]]
   [(re-frame/subscribe [:rows id])
    (re-frame/subscribe [:filters id])])
 (fn [[rows filter-fns]]
   (letfn [(apply-sort-filter [col-num col]
             (if-let [filters (vals (nth filter-fns col-num))]
               ((complement not-any?) true?
                                      (map (fn [filter-fn]
                                             (let [value (:value col)]
                                               (if-not (nil? (meta value))
                                                 (filter-fn (:sort-value (meta value)))
                                                 (filter-fn value)))) filters)) true))]
     (->> rows
          (filter #(not-any? false? (map-indexed apply-sort-filter %)))))))

(re-frame/reg-sub
 :treated-rows
 (fn [[_ id]]
   [(re-frame/subscribe [:filtered-rows id])
    (re-frame/subscribe [:sorted-column id])
    (re-frame/subscribe [:sort-ascending? id])])
 (fn [[rows sorted-column ascending]]
   (if (nil? sorted-column)
     rows
     (let [col (nth (first rows) (util/col-num sorted-column))
           f   (case (:type col)
                 (:map :string)
                 (if ascending
                   #(.localeCompare %1 %2)
                   #(.localeCompare %2 %1))
                 (if ascending < >))]
       (->> rows
            (sort-by
             (comp #(if-some [sort-value (:value (:value %))]
                      sort-value
                      (:value %)) #(nth % (util/col-num sorted-column))) f)
            (doall))))))

(re-frame/reg-sub :row-count sheet-extract)

(re-frame/reg-sub
 :virtual-range
 (fn [[_ id]]
   [(re-frame/subscribe [:state id :buffer-pos])
    (re-frame/subscribe [:state id :buffer-size])
    (re-frame/subscribe [:row-count id])])
 (fn [[buffer-pos buffer-size row-count]]
   (range (max 0 (int buffer-pos))
          (min row-count (int (+ buffer-pos buffer-size))))))

(re-frame/reg-sub
 :row
 (fn [[_ id]]
   [(re-frame/subscribe [:state id :buffer-pos])
    (re-frame/subscribe [:state id :buffer-size])
    (re-frame/subscribe [:treated-rows id])])
 (fn [[buffer-pos buffer-size rows] [_ id n]]
   (when (and (>= n buffer-pos)
              (<= n (+ buffer-pos buffer-size))
              (some? rows))
     (nth rows n []))))

(re-frame/reg-sub
 :unique-values
 (fn [db [_ id col-ref]]
   (get-in db (sheet-path [:unique-values (util/col-num col-ref)] id))))

#_(re-frame/reg-sub
   :unique-values
   (fn [[_ id col-ref]]
     (re-frame/subscribe [:column id col-ref]))
   (fn [{:keys [rows]}]
     (let [values      (->> rows (remove :title-row?) (map :value))
           sort-values (->> values
                            (map #(if-not (nil? (meta %))
                                    (:sort-value (meta %))
                                    %)))
           uniq        (->> sort-values
                            (distinct)
                            (sort <))]
       (if (vector? (first uniq))
         (map #(:sort-value (meta %)) uniq)
         uniq))))

;; Sorting ----------------------------------------------------------------


(re-frame/reg-sub
 :sort-ascending?
 (fn [[_ id]]
   (re-frame/subscribe [:state id :sort-column]))
 (fn [sorted-column-state _]
   (get sorted-column-state :sort-ascending?)))

(re-frame/reg-sub
 :sorted-column
 (fn [[_ id]]
   (re-frame/subscribe [:state id :sort-column]))
 (fn [sorted-column-state _]
   (get sorted-column-state :sorted-column)))

#_(re-frame/reg-sub
   :selection
   (fn [[_ id]]
     (re-frame/subscribe [:sheet id]))
   (fn [sheet _]
     (get sheet :selection)))

#_(re-frame/reg-sub
   :cells
   (fn [[_ id]]
     (re-frame/subscribe [:rows id]))
   (fn [rows _]
     (into (sorted-map)
           (map #(do {(:cell-ref %) (:value %)})
                (flatten rows)))))

#_(re-frame/reg-sub
   :cell
   (fn [[_ id]]
     (re-frame/subscribe [:cells id]))
   (fn [cells [_ id cell-ref]]
     (let [index (.indexOf (keys cells) (keyword cell-ref))]
       (nth (vals cells) index))))

(re-frame/reg-sub
 :range
 (fn [[_ id]]
   (re-frame/subscribe [:columns id]))
 (fn [cols [_ id from-ref to-ref]]
   (let [col-n  (util/col-num from-ref)
         from-n (util/row-num from-ref)
         to-n   (util/row-num to-ref)
         rows   (->> (nth cols col-n)
                     (:rows)
                     (drop from-n)
                     (take (inc to-n)))]
     rows)))
