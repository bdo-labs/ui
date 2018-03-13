(ns ui.element.numbers.helpers
  (:require [clojure.string :as str]
            #?(:cljs [goog.string :refer [format]])
            [ui.wire.polyglot :as polyglot]
            [ui.util :as util]))

(defn cell-ref
  "Generate a `cell-ref` from [col-ref] and [row-n]."
  [col-ref row-n]
  (keyword (str (name col-ref) row-n)))

;; TODO Reduce the type that's now available on each cell
(defn- infer-column
  "Infer the types of a column.
  Each column needs to be typed in order to perform correct sorting
  and filtering. The type-inference is based on the first, non
  title-row value, so if you try to mix and match values, you will
  encounter breakage."
  [col]
  (let [rows      (->> (:rows col) (remove :title-row?) (map :value))
        first-val (first rows)]
    (if-let [meta-type (:type (meta first-val))]
      meta-type
      (merge col (cond (number? first-val)
                       {:type :number
                        :min  (reduce min rows)
                        :max  (reduce max rows)}
                       (inst? first-val)
                       {:type :inst
                        :min  (reduce < rows)
                        :max  (reduce > rows)}
                       (string? first-val) {:type :string}
                       :else               {:type :any})))))

(defn- infer-cell
  "Infer the type of a cell."
  [cell]
  (cond (number? (:value cell)) :number
        (inst? (:value cell))   :inst
        (string? (:value cell)) :string
        (map? (:value cell))    :map
        (fn? (:value cell))     :fn
        :else                   :any))

(defn- format-value
  "Apply formatting to [value] based on the supplied [type]"
  [value type]
  (case type
    :number (polyglot/format-number-en (format "%.2f" value))
    :inst (polyglot/format-inst value)
    :map (or (:display-value value) (:value value))
    value))

(defn data->columns
  "Transform the [data] passed in; to manageable columns"
  [data]
  (->> (apply map list data)
       (map-indexed
        (fn [n col]
          (let [col-ref (util/col-refs n)
                col-n   (inc n)
                col     (vec (map-indexed
                              (fn [row-n cell]
                                (let [type (infer-cell cell)]
                                  (merge {:cell-ref      (cell-ref col-ref (inc row-n))
                                          :col-ref       col-ref
                                          :type          type
                                          :row-n         (inc row-n)
                                          :col-n         col-n
                                          :display-value (format-value (:value cell) type)} cell)))
                              col))]
            (infer-column {:col-ref col-ref
                           :filters {}
                           :rows    col}))))
       (vec)))

(defn csv-no-title->columns
  [{:keys [rows]}]
  (->> rows
       (mapv #(mapv (fn [col] {:value col :title-row? false}) %))))

(defn csv->columns
  [{:keys [title-rows rows] :as data}]
  (let [title-rows (->> title-rows
                        (mapv #(mapv (fn [col] {:value col :title-row? true}) %)))
        rows       (csv-no-title->columns data)]
    (into title-rows rows)))

(defn ->columns
  [[k v]]
  (data->columns
   (case k
     :csv          (csv->columns v)
     :csv-no-title (csv-no-title->columns v)
     v)))

(defn sheet-path
  ([id]
   (into [:sheet (util/slug id)]))
  ([path id]
   (into [:sheet (util/slug id)] path)))

(defn sheet-extract [db [k id]]
  (get-in db (sheet-path [k] id) nil))

