(ns ui.wire.form
  (:require [clojure.spec.alpha :as spec]
            #?(:cljs [reagent.core :refer [atom] :as reagent])
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.wire.form.table :as form.table]
            [ui.util :as util]))


(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))
(spec/def ::on-valid (spec/or :fn? fn? :keyword? keyword?))

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

(defn- get-validation-errors [form-map old-state]
  (fn [out [k v]]
    (let [field (get-in form-map [:fields k])]
      ;; when there is a spec
      ;; AND, there has been a change in value
      (if (and (:spec field)
               (not= v (get old-state k)))
        (if (spec/valid? (:spec field) v)
          ;; if the spec is valid we change it to hold zero errors
          (conj out [k []])
          ;; if the spec is invalid we give an explanation to
          ;; what's wrong
          (conj out [k (spec/explain-str (:spec field) v)]))
        ;; if not true, just pass along out as normal
        out))))

(defn- add-validation-watcher
  "Add validation checks for the RAtom as it changes"
  [form-map]
  (let [{{on-valid :on-valid} :options} form-map]
    (add-watch (:data form-map) (str "form-watcher-" (:id form-map))
               (fn [_ _ old-state new-state]
                 ;; get all errors for all fields
                 (let [field-errors (reduce (get-validation-errors form-map old-state) [] new-state)]
                   ;; update the RAtoms for the error map
                   (doseq [[k errors] field-errors]
                     (reset! (get-in form-map [:errors k]) errors))
                   ;; if there are no errors then the form is valid and we can fire off the function
                   (when (every? empty? (map second field-errors))
                     (if (fn? on-valid)
                       (on-valid new-state form-map)
                       (re-frame/dispatch [on-valid new-state form-map]))))))))

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
                             (assoc out name (:value field)))
                           {} map-fields))
        form-map {:fields  map-fields
                  :options options
                  :errors  errors
                  :data data}]
    #_(add-watch (:data form-map) :foobar
               (fn [k _ old-state new-state]
                 (println new-state)))
    (add-validation-watcher form-map)
    form-map))

(def as-table form.table/as-table)

(defmacro defform [-name options fields]
  (let [form-name (name -name)]
    `(defn ~-name
       ([~'data]
        (~-name ~'data nil))
       ([~'data ~'opts]
        (form ~fields (assoc ~options :form-name ~form-name) ~'opts ~'data)))))
