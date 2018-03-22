(ns ui.wire.form.table
  (:require [clojure.spec.alpha :as spec]
            [ui.wire.form.common :as common]
            [ui.wire.wiring :as wiring]
            [ui.util :as util]))


(spec/def ::form map?)
(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))

(spec/def ::table-params
  (spec/keys :opt-un [::id]))

(spec/def ::table-args (spec/cat :params ::table-params :form-map ::form))


(defn- table-row [{:keys [wiring] :as field} {{label? :label?} :options :as form-map}]
  (if wiring
    [:$wrapper wiring]
    (if (false? label?)
      [:tr {:key (str "tr-" (:id field))}
       [:td
        (common/render-field field form-map)
        (common/render-error-element field form-map)
        (common/render-text field form-map)
        (common/render-help field form-map)]]
      [:tr {:key (str "tr-" (:id field))}
       [:td
        (common/render-label field form-map)]
       [:td
        (common/render-field (dissoc field :label) form-map)
        (common/render-error-element field form-map)
        (common/render-text field form-map)
        (common/render-help field form-map)]])))

(defn as-table
  [& args]
  (let [{:keys [params form-map]} (util/conform! ::table-args args)
        {:keys [id]
         :or   {id (util/gen-id)}} params
        ;; generate the body of the table
        body (common/get-body table-row params form-map)]
    (fn [& args]
      (let [{:keys [params]} (util/conform! ::table-args args)
            {:keys [style
                    class]
             :or {style {}
                  class ""}} params]
        [:table {:key (util/slug "form-table" id)
                 :style style
                 :class class}
         [:tbody body]]))))
