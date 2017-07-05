(ns ui.element.numbers.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [ui.util :as u]))


(reg-sub :initialized?
         (fn [[_ sheet-ref]]
           (subscribe [:sheet sheet-ref]))
         (fn [sheet _]
           (get sheet :initialized? false)))


(reg-sub :sheet
         (fn [db [_ sheet-ref]]
           (get db sheet-ref)))


(reg-sub :columns
         (fn [[_ sheet-ref]]
           (subscribe [:sheet sheet-ref]))
         (fn [sheet _]
           (get sheet :columns)))


(reg-sub :column
         (fn [[_ sheet-ref]]
           (subscribe [:columns sheet-ref]))
         (fn [columns [_ _ col-ref]]
           (->> columns
                (filterv #(= (:col-ref %) col-ref))
                (first))))


(reg-sub :unique-values
         (fn [[_ sheet-ref col-ref]]
           (subscribe [:column sheet-ref col-ref]))
         (fn [column]
           (let [uniq (->> (:rows column)
                           (remove :title-row?)
                           (map :value)
                           (distinct)
                           (sort <))]
             (if (vector? (first uniq))
               (map #(:sort-value (meta %)) uniq)
               uniq))))


;; Filtering
;; FIXME It's not really obvious how this works, needs a bit of clean-up
(defn filter-rows [columns _]
  (when (not-empty columns)
    (let [filter-fns (map :filters columns)]
      (->> columns
           (map :rows)
           (apply map list)
           (remove #(:title-row? (first %)))
           (filter #(not-any? false?
                              (map-indexed
                               (fn [col-num col]
                                 (if-let [filters (vals (nth filter-fns col-num))]
                                   ((complement not-any?) true?
                                    (map (fn [filter-fn]
                                           (filter-fn (:value col))) filters))
                                   true))
                               %)))))))


(reg-sub :filtered-rows
         (fn [[_ sheet-ref]]
           (subscribe [:columns sheet-ref]))
         filter-rows)


;; Sorting
(reg-sub :sort-ascending?
         (fn [[_ sheet-ref]]
           (subscribe [:sheet sheet-ref]))
         (fn [sheet _]
           (get sheet :sort-ascending?)))


(reg-sub :sorted-column
         (fn [[_ sheet-ref]]
           (subscribe [:sheet sheet-ref]))
         (fn [sheet _]
           (get sheet :sorted-column)))


(reg-sub :show-column-menu?
         (fn [[_ sheet-ref]]
           (subscribe [:sheet sheet-ref]))
         (fn [sheet [_ _ col-ref]]
           (= col-ref (get sheet :show-column-menu))))


(reg-sub :col-refs
         (fn [[_ sheet-ref]]
           (subscribe [:columns sheet-ref]))
         (fn [columns _]
           (map :col-ref columns)))


(reg-sub :selection
         (fn [[_ sheet-ref]]
           (subscribe [:sheet sheet-ref]))
         (fn [sheet _]
           (get sheet :selection)))


(reg-sub :title-rows
         (fn [[_ sheet-ref]]
           (subscribe [:columns sheet-ref]))
         (fn [columns _]
           (when (not-empty columns)
             (let [rows (->> columns
                             (map :rows)
                             (apply map list)
                             (map #(filter :title-row? %))
                             (remove empty?))]
               rows))))


(reg-sub :rows
         (fn [[_ sheet-ref]]
           [(subscribe [:filtered-rows sheet-ref])
            (subscribe [:sorted-column sheet-ref])
            (subscribe [:sort-ascending? sheet-ref])])
         (fn [[filtered-rows sorted-column sort-ascending?] _]
           (let [sort-fn (if sort-ascending? > <)]
             (if (nil? sorted-column)
               filtered-rows
               (->> filtered-rows
                    (sort-by (comp :value #(nth % (u/col-num sorted-column)))
                             sort-fn))))))


(reg-sub :cells
         (fn [[_ sheet-ref]]
           (subscribe [:rows sheet-ref]))
         (fn [rows _]
           (into (sorted-map)
                 (map #(do {(:cell-ref %) (:value %)})
                      (flatten rows)))))


(reg-sub :cell
         (fn [[_ sheet-ref]]
           (subscribe [:cells sheet-ref]))
         (fn [cells [_ sheet-ref cell-ref]]
           (let [index (.indexOf (keys cells) (keyword cell-ref))]
             (nth (vals cells) index))))
