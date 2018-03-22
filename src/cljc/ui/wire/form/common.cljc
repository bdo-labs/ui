(ns ui.wire.form.common
  #?(:cljs (:require [reagent.core :as reagent])))


(defn render-error-element [{:keys [error-element name] :as field} form-map]
  (when-not (#{:dispatch} error-element)
    [error-element {:model (get-in form-map [:errors name])}]))

(defn render-field [{:keys [field-fn name] :as field} form-map]
  [field-fn (assoc field :model #?(:cljs (reagent/cursor (:data form-map) [name])
                                   :clj  (get-in form-map [:data name])))])


(defn render-text [{:keys [field-fn text] :as field} form-map]
  (if text
    [:div.Text text]))

(defn render-help [{:keys [field-fn help] :as field} form-map]
  (if help
    [:div.Help help]))

(defn render-label [field form-map]
  [:label {:for (:id field)} (or (:label field) (:name field))])
