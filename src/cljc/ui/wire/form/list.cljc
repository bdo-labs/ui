(ns ui.wire.form.list
  (:require [clojure.spec.alpha :as spec]
            [ui.wire.form.common :as common]
            [ui.wire.wiring :as wiring]
            [ui.util :as util]))


(spec/def ::form map?)
(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))
(spec/def ::type #{:ul :ol})


(spec/def ::list-params
  (spec/keys :opt-un [::id
                      ::common/wiring
                      ::type]))

(spec/def ::list-args (spec/cat :params ::list-params :form-map ::form))


(defn- li [{:keys [wiring] :as field} {{label? :label?} :options :as form-map}]
  (if wiring
    [:$wrapper wiring]
    (if (false? label?)
      [:li {:key (str "li-" (:id field))}
       (common/render-field field form-map)
       (common/render-error-element field form-map)
       (common/render-text field form-map)
       (common/render-help field form-map)]
      [:li {:key (str "li-" (:id field))}
       (common/render-label field form-map)
       (common/render-field (dissoc field :label) form-map)
       (common/render-error-element field form-map)
       (common/render-text field form-map)
       (common/render-help field form-map)])))

(defn as-list
  [& args]
  (let [{:keys [params form-map]} (util/conform! ::list-args args)
        {:keys [id type]
         :or   {id   (util/gen-id)
                type :ul}} params
        body (common/get-body li params form-map)]
    (fn [& args]
      (let [{:keys [params]} (util/conform! ::list-args args)
            {:keys [style
                    class]
             :or {style {}
                  class ""}} params]
        [type {:key (util/slug "form-list" id)
               :style style
               :class class}
         body]))))
