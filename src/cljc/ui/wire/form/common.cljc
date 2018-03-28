(ns ui.wire.form.common
  (:require [clojure.spec.alpha :as spec]
            #?(:cljs [reagent.core :as reagent])
            [ui.wire.wiring :as wiring]))


;; allow override of wiring per rendering type (table, list, etc)
(spec/def ::wiring (spec/map-of keyword? any?))
;; allow override of everything
(spec/def ::template vector?)


(defn render-error-element [{:keys [error-element name] :as field} form-map]
  (when-not (#{:dispatch} error-element)
    [error-element {:model (get-in form-map [:errors name])}]))

(defn render-field [{:keys [field-fn name] :as field} form-map]
  [field-fn (-> field
                (assoc :model #?(:cljs (reagent/cursor (:data form-map) [name])
                                 :clj  (get-in form-map [:data name])))
                (dissoc :wiring :template))])


(defn render-text [{:keys [field-fn text] :as field} form-map]
  (if text
    [:div.Text text]))

(defn render-help [{:keys [field-fn help] :as field} form-map]
  (if help
    [:div.Help help]))

(defn render-label [field form-map]
  [:label {:for (:id field)} (or (:label field) (:name field))])


(defn assoc-wiring [{:keys [name] :as field} params]
  (let [params-wiring (:wiring params)]
    (assoc field :wiring
           (cond (contains? params-wiring name)
                 (get params-wiring name)

                 :else
                 (:wiring field)))))

(defn get-body [row-fn params form-map]
  (map (fn [field]
         (let [field (assoc-wiring field params)
               ;; fetch the row
               row (row-fn field form-map)]
           ;; if we have wiring or label-wiring for the field we replace it using wiring

           (cond (:wiring field)
                 (wiring/wiring row {:$wrapper wiring/unwrapper
                                     :$key     {:key (str "ui-wire-form-common-" (:id field))}
                                     :$label   (render-label field form-map)
                                     :$field   (render-field (dissoc field :label) form-map)
                                     :$errors  (render-error-element field form-map)
                                     :$text    (render-text field form-map)
                                     :$help    (render-help field form-map)
                                     })


                 ;; otherwise we're good to go with use the default row
                 :else
                 row)))
       (map second (:fields form-map))))
