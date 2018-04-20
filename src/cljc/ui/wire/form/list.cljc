(ns ui.wire.form.list
  (:require [clojure.spec.alpha :as spec]
            [ui.specs :as specs.common]
            [ui.wire.form.common :as common]
            [ui.wire.form.helpers :as helpers]
            [ui.util :as util]))


(spec/def ::type #{:ul :ol})


(spec/def ::params
  (spec/keys :opt-un [::specs.common/id
                      ::common/wiring
                      ::type]))

(spec/def ::args (spec/cat :params ::params :form-map ::common/form :content (spec/? ::common/content)))


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
  (let [{:keys [params form-map content]} (util/conform! ::args args)
        {:keys [id type]
         :or   {id   (util/gen-id)
                type :ul}} params
        body (common/get-body li params form-map)
        re-render? (helpers/re-render? form-map)]
    (fn [& args]
      (let [{:keys [params form-map]} (util/conform! ::args args)
            {:keys [style
                    class]
             :or {style {}
                  class ""}} params
            body (if re-render? (common/get-body li params form-map) body)]
        [type {:key (util/slug "form-list" id)
               :style style
               :class class}
         body
         (if content
           [content])]))))
