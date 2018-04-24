(ns ui.wire.form
  (:require [clojure.spec.alpha :as spec]
            #?(:cljs [reagent.core :refer [atom] :as reagent])
            [phrase.alpha :refer [phrase]]
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.wire.form.helpers :as form.helpers]
            [ui.wire.form.list :as form.list]
            [ui.wire.form.paragraph :as form.paragraph]
            [ui.wire.form.table :as form.table]
            [ui.wire.form.template :as form.template]
            [ui.wire.form.wizard :as form.wizard]
            [ui.wire.form.wire :as form.wire]
            [ui.util :as util]))


(re-frame/reg-sub      ::error       (fn [db [_ id field-name]]
                                       (get-in db [::error id field-name] [])))
(re-frame/reg-sub      ::on-valid    (fn [db [_ id]]
                                       (get-in db [::on-valid id] ::invalid)))
(re-frame/reg-sub      ::wizard-current-step (fn [db [_ id]]
                                               (get-in db [::wizard id :current-step])))

(re-frame/reg-event-db ::error       (fn [db [_ id field-name errors]]
                                       (assoc-in db [::error id field-name] errors)))
(re-frame/reg-event-db ::on-valid    (fn [db [_ id new-state]]
                                       (assoc-in db [::on-valid id] new-state)))
(re-frame/reg-event-db ::wizard-current-step (fn [db [_ id step]]
                                               (assoc-in db [::wizard id :current-step] step)))


(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))
(spec/def ::on-valid (spec/or :fn? fn? :dispatcher #{:dispatch}))

(spec/def ::error-element (spec/or :fn? fn? :dispatcher #{:dispatch}))
(spec/def ::text any?)
(spec/def ::help any?)
(spec/def ::wiring any?)
(spec/def ::field (spec/keys :opt-un [::error-element
                                      ::wiring
                                      ::text
                                      ::help]))
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
(defmethod get-field-fn :fn [field]
  (:type field))
(defmethod get-field-fn :keyword [field]
  (case (:type field)
    ::element/textfield     element/textfield
    ::element/numberfield   element/numberfield
    ::element/dropdown      element/dropdown
    ::element/checkbox      element/checkbox
    ::element/chooser       element/chooser
    ;; ::element/days          element/days
    ;; ::element/months        element/months
    ;; ::element/date-picker   element/date-picker
    ;; ::element/period-picker element/period-picker
    nil))
(defmethod get-field-fn :default [field]
  (:type field))

(defn- get-error-messages [field value]
  (let [explanation (spec/explain-data (:spec field) value)
        messages (mapv #(or (phrase {} %) (str %)) (::spec/problems explanation))]
    messages))

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
           (conj out [k same-value? (get-error-messages field v)])))
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
                       (re-frame/dispatch [::error (:id form-map) k errors])
                       (reset! (get-in form-map [:errors k]) errors)))
                   ;; if there are no errors then the form is valid and we can fire off the function
                   (let [valid? (every? empty? (map last field-errors))
                         to-send (if valid? new-state ::invalid)]
                     (when (fn? on-valid)
                       (on-valid to-send))
                     (re-frame/dispatch [::on-valid (:id form-map) to-send])))))))

(defn- get-default-value [field]
  (let [value (or (:value field) (util/deref-or-value (:model field)))]
    (condp isa? (:field-fn field)
      element/textfield (or value "")
      element/chooser   (or value #{})
      element/checkbox  (or value :not-checked)
      value)))

(defrecord Form [fields field-ks options id errors data meta])
(defn form [fields form-options override-options data]
  ;; do the conform here as conform can change the structure of the data
  ;; that comes out in order to show how it came to that conclusion (spec/or for example)
  (util/conform! ::form-args (list fields form-options override-options data))
  (let [options (merge {:id (util/gen-id)} form-options override-options)
        map-fields (->> fields
                        (map (fn [{:keys [name id error-element] :as field}]
                               [name (assoc field
                                            :field-fn (get-field-fn field)
                                            :error-element (or error-element element/notifications)
                                            ;; always generate id in the form so we
                                            ;; can reference it later
                                            :id (or id (util/gen-id)))]))
                        (into (array-map)))
        errors (reduce (fn [out [name _]]
                         (assoc out name (atom [])))
                       {} map-fields)
        data (atom (reduce (fn [out [name field]]
                             (assoc out name (get-default-value field)))
                           {} map-fields))
        form-map (map->Form {:fields  map-fields
                             ;; field-ks control which fields are to be rendered for
                             ;; everything form supports with the exception of wiring
                             :field-ks (mapv :name fields)
                             :options options
                             :id      (:id options)
                             :errors  errors
                             :data    data})]
    (add-validation-watcher form-map)
    form-map))

(def as-table         (form.wizard/wizard form.table/as-table))
(def as-list          (form.wizard/wizard form.list/as-list))
(def as-paragraph     (form.wizard/wizard form.paragraph/as-paragraph))
(def as-template      (form.wizard/wizard form.template/as-template))
(def as-wire          form.wire/as-wire)
(def valid?           form.helpers/valid?)
(def button           form.helpers/button)
(def table-button     form.helpers/table-button)
(def list-button      form.helpers/list-button)
(def paragraph-button form.helpers/paragraph-button)


(defmacro defform [-name options fields]
  (let [form-name (name -name)]
    `(defn ~-name
       ([~'data]
        (~-name nil ~'data))
       ([~'opts ~'data]
        (form ~fields (assoc ~options :form-name ~form-name) ~'opts ~'data)))))
