(ns ui.element.form
  (:require [clojure.spec.alpha :as spec]
            #?(:cljs [reagent.core :refer [atom] :as reagent])
            [ui.elements :as element]
            [ui.util :as util]))


(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))
(spec/def ::on-valid (spec/or :fn? fn? :keyword? keyword?))
(spec/def ::form map?)

(spec/def ::table-params
  (spec/keys :opt-un [::id]
             :req-un [::on-valid]))

(spec/def ::table-args (spec/cat :params ::table-params :form-map ::form))

(defmulti ^:private get-field-fn (fn [field]
                                   (cond (fn? (:type field))      :fn
                                         (keyword? (:type field)) :keyword
                                         :else                    nil)))
(defmethod get-field-fn :keyword [field]
  (case (:type field)
    ::element/textfield element/textfield
    ::element/numberfield element/numberfield
    nil))
(defmethod get-field-fn :default [field]
  (:type field))


(defn form [fields form-options override-options data]
  (let [options (merge form-options override-options)
        map-fields (->> fields
                        (map (fn [{:keys [name id] :as field}]
                               [name (assoc field
                                            :field-fn (get-field-fn field)
                                            ;; always generate id in the form so we
                                            ;; can reference it later
                                            :id (or id (util/gen-id)))]))
                        (into (sorted-map)))
        errors (reduce (fn [out [name _]]
                         (assoc out name (atom [])))
                       {} map-fields)
        data (atom (reduce (fn [out [name field]]
                             (assoc out name :value fields))
                           {} map-fields))
        form-map {:fields  map-fields
                  :options options
                  :errors  errors
                  :data data}]
    (add-watch (:data form-map) :foobar
               (fn [k _ old-state new-state]
                 (println new-state)))
    form-map))

(comment
  (-> [{:type ::element/numberfield
       :name :number1}
      {:type element/numberfield
       :name :number2}]
     (form {} {} {})
     (println))
  )

(defn render-field [{:keys [field-fn name] :as field} form-map]
  [field-fn (assoc field :model #?(:cljs (reagent/cursor (:data form-map) [name])
                                   :clj  (get-in form-map [:data name])))])


(defn- table-row [field {:keys [label?] :as form-map}]
  (if-not label?
    [:tr {:key (str "tr-" (:id field))} [:td (render-field field form-map)]]
    [:tr {:key (str "tr-" (:id field))}
     [:td [:label {:for (:id field)} (or (:label field) (:name field))]]
     [:td (render-field (dissoc field :label) form-map)]]))

(defn as-table
  [& args]
  (let [{:keys [params form-map]} (util/conform! ::table-args args)
        {:keys [id]
         :or   {id (util/gen-id)}} params]
    (fn [& args]
      (let [{:keys [params]} (util/conform! ::table-args args)
            {:keys [style
                    class]
             :or {style {}
                  class ""}} params]
        [:table {:key (util/slug "form-table" id)
                 :style style
                 :class class}
         [:tbody (map #(table-row % form-map) (map second (:fields form-map)))]]))))


(defmacro defform [-name options fields]
  (let [form-name (name -name)]
    `(defn ~-name
       ([~'data]
        (~-name ~'data nil))
       ([~'data ~'opts]
        (form ~fields (assoc ~options :form-name ~form-name) ~'opts ~'data)))))
