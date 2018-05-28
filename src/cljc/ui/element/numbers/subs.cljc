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

(re-frame/reg-sub
 :table-height
 (fn [[_ id]]
   [(re-frame/subscribe [:state id :row-height])
    (re-frame/subscribe [:row-count id])])
 (fn [[row-height row-count] _]
   (when (some? row-height)
     (* row-height row-count))))


;; Table Headings ---------------------------------------------------------

(re-frame/reg-sub :title-rows sheet-extract)

(re-frame/reg-sub
 :visible-title-rows
 (fn [[_ id]]
   [(re-frame/subscribe [:title-rows id])
    (re-frame/subscribe [:state id :hide-columns])])
 (fn [[title-rows hide]]
   (let [hidden (fn [col] (contains? hide (:col-ref col)))]
     (->> title-rows
          (map #(remove hidden %))))))

;; Table Columns ----------------------------------------------------------

(re-frame/reg-sub :columns sheet-extract)

(re-frame/reg-sub
 :column
 (fn [db [_ id col-ref]]
   (let [path (sheet-path [:columns (util/col-num col-ref)] id)]
     (get-in db path))))

(re-frame/reg-sub
 :column-count
 (fn [db [_ id]]
   (let [path (sheet-path [:column-count] id)]
     (get-in db path))))

(re-frame/reg-sub
 :visible-columns
 (fn [[_ id]]
   [(re-frame/subscribe [:columns id])
    (re-frame/subscribe [:state id :hide-columns])])
 (fn [[columns hide]]
   (->> columns (remove #(contains? hide (:col-ref %))))))

(re-frame/reg-sub
 :col-refs
 (fn [[_ id]] (re-frame/subscribe [:columns id]))
 (fn [columns _] (map :col-ref columns)))

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

(re-frame/reg-sub
 :filters
 (fn [[_ id]]
   (re-frame/subscribe [:columns id]))
 (fn [columns [_ id]]
   (map :filters columns)))

(re-frame/reg-sub
 :checkboxes
 (fn [[_ id]]
   (re-frame/subscribe [:columns id]))
 (fn [columns [_ _ col-ref]]
   (let [path [(util/col-num col-ref) :checkboxes]]
     (get-in columns path))))

;; Table Rows -------------------------------------------------------------

(re-frame/reg-sub
 :rows
 (fn [[_ id]]
   (re-frame/subscribe [:columns id]))
 (fn [columns _]
   (when (seq columns)
    (->> columns
         (map :rows)
         (apply map list)
         (remove #(:title-row? (first %)))))))

(re-frame/reg-sub
 :filtered-rows
 (fn [[_ id]]
   [(re-frame/subscribe [:rows id])
    (re-frame/subscribe [:filters id])])
 (fn [[rows filter-fns]]
   (when (and (seq rows)
              (seq filter-fns))
     (let [any? (complement not-any?)]
       (letfn [(apply-sort-filter [col-num col]
                 (if-let [filters (vals (nth filter-fns col-num))]
                   (any? true?
                         (map (fn [filter-fn]
                                (let [value (:display-value col)]
                                  (if (and (some? (meta value))
                                           (some? (:sort-value (meta value))))
                                    (filter-fn (:sort-value (meta value)))
                                    (filter-fn value)))) filters)) true))
               (only-true? [row]
                 (not-any? false? (map-indexed apply-sort-filter row)))]
         (let [xf (filter only-true?)]
           (transduce xf conj rows)))))))

(re-frame/reg-sub
 :sorted-rows
 (fn [[_ id]]
   [(re-frame/subscribe [:filtered-rows id])
    (re-frame/subscribe [:sorted-column id])
    (re-frame/subscribe [:sort-ascending? id])])
 (fn [[rows sorted-column ascending]]
   (when (seq rows)
    (if (nil? sorted-column)
      rows
      (let [col (nth (first rows) (util/col-num sorted-column))
            f   (case (:type col)
                  (:map :string) (if ascending
                                   #(.localeCompare %1 %2)
                                   #(.localeCompare %2 %1))
                  (if ascending < >))
            comparator (comp #(if-some [sort-value (:value (:value %))]
                                sort-value
                                (:value %))
                             #(nth % (util/col-num sorted-column)))]
        (sort-by comparator f rows))))))

(re-frame/reg-sub
 :row-count
 (fn [[_ id]]
   (re-frame/subscribe [:filtered-rows id]))
 (fn [rows _]
   (if (seq rows) (count rows) 0)))

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
    (re-frame/subscribe [:sorted-rows id])])
 (fn [[buffer-pos buffer-size rows] [_ id n]]
   (when (and (>= n buffer-pos)
              (<= n (+ buffer-pos buffer-size))
              (seq rows))
     (nth rows n []))))

(re-frame/reg-sub
 :unique-values
 (fn [db [_ id col-ref]]
   (let [path (sheet-path [:unique-values (util/col-num col-ref)] id)]
     (get-in db path []))))

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
