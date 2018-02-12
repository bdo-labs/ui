(ns ui.element.numbers.subs
  (:require [re-frame.core :as re-frame]
            [ui.util :as util]))


;; Helper functions -------------------------------------------------------


(defn sheet-path [path ref]
  (concat [ref] path))


(def columns-path (partial sheet-path [:columns]))


(defn column-path [sheet-ref col-ref]
  (let [col-num (util/col-num col-ref)]
    (conj (columns-path sheet-ref) col-num)))


;; Subscriptions ----------------------------------------------------------


(re-frame/reg-sub
 :sheet
 (fn [db [_ sheet-ref]]
   (get db sheet-ref)))


(re-frame/reg-sub
 :columns
 (fn [db [_ sheet-ref]]
   (get-in db (columns-path sheet-ref))))


(re-frame/reg-sub
 :column
 (fn [db [_ sheet-ref col-ref]]
   (get-in db (column-path sheet-ref col-ref))))


(re-frame/reg-sub
 :num-columns
 (fn [db [_ sheet-ref]]
   (count (get-in db (columns-path sheet-ref)))))


(re-frame/reg-sub
 :state
 (fn [db [k sheet-ref param]]
   (get-in db [sheet-ref k param] nil)))


(re-frame/reg-sub
 :checkbox
 (fn [db [_ sheet-ref col-ref checkbox-id]]
   (get-in db (conj (column-path sheet-ref col-ref) :checkboxes checkbox-id))))


(re-frame/reg-sub
 :query
 (fn [db [_ sheet-ref col-ref]]
   (get-in db (conj (column-path sheet-ref col-ref) :query) nil)))


(re-frame/reg-sub
 :unique-values
 (fn [[_ sheet-ref col-ref]]
   (re-frame/subscribe [:column sheet-ref col-ref]))
 (fn [column]
   (let [values      (->> (:rows column)
                        (remove :title-row?)
                        (map :value))
         sort-values (->> values
                        (map #(if-not (nil? (meta %))
                                (:sort-value (meta %))
                                %)))
         uniq        (->> sort-values
                        (distinct)
                        (sort <))]
     #_(if (vector? (first uniq))
         (map #(:sort-value (meta %)) uniq)
         uniq)
     uniq)))


;; Filtering --------------------------------------------------------------


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
 :filtered-rows
 (fn [[_ sheet-ref]]
   (re-frame/subscribe [:columns sheet-ref]))
 filter-rows)


;; Sorting ----------------------------------------------------------------


(re-frame/reg-sub
 :sort-ascending?
 (fn [[_ sheet-ref]]
   (re-frame/subscribe [:sheet sheet-ref]))
 (fn [sheet _]
   (get sheet :sort-ascending?)))


(re-frame/reg-sub
 :sorted-column
 (fn [[_ sheet-ref]]
   (re-frame/subscribe [:sheet sheet-ref]))
 (fn [sheet _]
   (get sheet :sorted-column)))





(re-frame/reg-sub
 :show-column-menu?
 (fn [[_ sheet-ref]]
   (re-frame/subscribe [:sheet sheet-ref]))
 (fn [sheet [_ _ col-ref]]
   (= col-ref (get sheet :show-column-menu))))


(re-frame/reg-sub
 :col-refs
 (fn [[_ sheet-ref]]
   (re-frame/subscribe [:columns sheet-ref]))
 (fn [columns _]
   (map :col-ref columns)))


(re-frame/reg-sub
 :selection
 (fn [[_ sheet-ref]]
   (re-frame/subscribe [:sheet sheet-ref]))
 (fn [sheet _]
   (get sheet :selection)))


(re-frame/reg-sub
 :title-rows
 (fn [[_ sheet-ref]]
   (re-frame/subscribe [:columns sheet-ref]))
 (fn [columns _]
   (when (not-empty columns)
     (let [rows (->> columns
                   (map :rows)
                   (apply map list)
                   (map #(filter :title-row? %))
                   (remove empty?))]
       rows))))


(re-frame/reg-sub
 :rows
 (fn [[_ sheet-ref]]
   [(re-frame/subscribe [:filtered-rows sheet-ref])
    (re-frame/subscribe [:sorted-column sheet-ref])
    (re-frame/subscribe [:sort-ascending? sheet-ref])])
 (fn [[filtered-rows sorted-column sort-ascending?] _]
   (let [sort-fn (if sort-ascending? < >)]
     (if (nil? sorted-column)
       filtered-rows
       (->> filtered-rows
          (sort-by (comp #(if-some [m (meta (:value %))]
                         (:sort-value m)
                         (:value %)) #(nth % (util/col-num sorted-column)))
                   sort-fn))))))


#_(re-frame/reg-sub
 :row-count
 (fn [[_ sheet-ref]]
   (re-frame/subscribe [:rows sheet-ref]))
 (fn [rows _]
   (count rows)))


(re-frame/reg-sub
 :row
 (fn [[_ sheet-ref]]
   [(re-frame/subscribe [:rows sheet-ref])])
 (fn [[rows] [_ sheet-ref n]]
   (when (some? rows)
     (nth rows n))))


(re-frame/reg-sub
 :cells
 (fn [[_ sheet-ref]]
   (re-frame/subscribe [:rows sheet-ref]))
 (fn [rows _]
   (into (sorted-map)
         (map #(do {(:cell-ref %) (:value %)})
              (flatten rows)))))


(re-frame/reg-sub
 :cell
 (fn [[_ sheet-ref]]
   (re-frame/subscribe [:cells sheet-ref]))
 (fn [cells [_ sheet-ref cell-ref]]
   (let [index (.indexOf (keys cells) (keyword cell-ref))]
     (nth (vals cells) index))))


(re-frame/reg-sub
 :range
 (fn [[_ sheet-ref]]
   (re-frame/subscribe [:columns sheet-ref]))
 (fn [cols [_ sheet-ref from-ref to-ref]]
   (let [col-n  (util/col-num from-ref)
         from-n (util/row-num from-ref)
         to-n   (util/row-num to-ref)
         rows   (->> (nth cols col-n)
                   (:rows)
                   (drop from-n)
                   (take (inc to-n)))]
     rows)))
