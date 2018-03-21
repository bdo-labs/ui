(ns ui.wire.form.table
  (:require [clojure.spec.alpha :as spec]
            [ui.wire.form.common :as common]
            [ui.util :as util]))


(spec/def ::form map?)

(spec/def ::table-params
  (spec/keys :opt-un [::id]
             :req-un [::on-valid]))

(spec/def ::table-args (spec/cat :params ::table-params :form-map ::form))


(defn- table-row [field {{label? :label?} :options :as form-map}]
  (if (false? label?)
    [:tr {:key (str "tr-" (:id field))}
     [:td (common/render-field field form-map)
          (common/render-error-element field form-map)]]
    [:tr {:key (str "tr-" (:id field))}
     [:td [:label {:for (:id field)} (or (:label field) (:name field))]]
     [:td (common/render-field (dissoc field :label) form-map)
          (common/render-error-element field form-map)]]))

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
