(ns ui.wire.form
  (:require [clojure.spec.alpha :as spec]
            #?(:cljs [reagent.core :refer [atom] :as reagent])
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.wire.form.table :as form.table]
            [ui.util :as util]))


(re-frame/reg-sub      ::error (fn [db [_ id field-name]]
                                 (get-in db [::error id field-name])))

(re-frame/reg-event-db ::error (fn [db [_ id field-name errors]]
                                 (assoc-in db [::error id field-name] errors)))


(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))
(spec/def ::on-valid (spec/or :fn? fn? :keyword? keyword?))

(spec/def ::error-element (spec/or :fn? fn? :dispatcher #{:dispatch}))
(spec/def ::field (spec/keys :opt-un [::error-element]))
(spec/def ::fields (spec/coll-of ::field))

(spec/def ::form-options (spec/keys :opt-un [::on-valid]))
(spec/def ::override-options map?)
(spec/def ::data map?)

(spec/def ::form-args (spec/cat :fields ::fields
                                :form-options ::form-options
                                :override-options ::override-options
                                :data ::data))

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
      (if (:spec field)
        ;; same-value? is used to determine if we should do updates to errors
        ;; we still need all the errors for on-valid to properly fire
        (let [same-value? (= v (get old-state k))]
          (if (spec/valid? (:spec field) v)
           ;; if the spec is valid we change it to hold zero errors
           (conj out [k same-value? []])
           ;; if the spec is invalid we give an explanation to
           ;; what's wrong
           (conj out [k same-value? [(spec/explain-str (:spec field) v)]])))
        ;; no spec defined, give back the out value
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
                   (doseq [[k same-value? errors] field-errors]
                     (when-not same-value?
                       (if (#{:dispatch} (get-in form-map [:fields k :error-element]))
                         (re-frame/dispatch [::error (:id form-map) k errors])
                         (reset! (get-in form-map [:errors k]) errors))))
                   ;; if there are no errors then the form is valid and we can fire off the function
                   (when (every? empty? (map last field-errors))
                     (if (fn? on-valid)
                       (on-valid new-state form-map)
                       (re-frame/dispatch [on-valid new-state form-map]))))))))

(defn form [fields form-options override-options data]
  ;; do the conform here as conform can change the structure of the data
  ;; that comes out in order to show how it came to that conclusion (spec/or for example)
  (util/conform! ::form-args (list fields form-options override-options data))
  (let [options (merge form-options override-options)
        map-fields (->> fields
                        (map (fn [{:keys [name id error-element] :as field}]
                               [name (assoc field
                                            :field-fn (get-field-fn field)
                                            :error-element (or error-element element/notifications)
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
