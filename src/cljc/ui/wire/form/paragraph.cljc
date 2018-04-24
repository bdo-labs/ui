(ns ui.wire.form.paragraph
  (:require [clojure.spec.alpha :as spec]
            [ui.specs :as specs.common]
            [ui.wire.form.common :as common]
            [ui.wire.form.helpers :as helpers]
            [ui.util :as util]))


(spec/def ::params
  (spec/keys :opt-un [::specs.common/id
                      ::common/wiring]))

(spec/def ::args (spec/cat :params ::params :form-map ::common/form :content (spec/? ::common/content)))


(defn- paragraph [{:keys [wiring] :as field} {{label? :label?} :options :as form-map}]
  (if wiring
    [:$wrapper wiring]
    (if (false? label?)
      [:p {:key (str "li-" (:id field))}
       (common/render-field field form-map)
       (common/render-error-element field form-map)
       (common/render-text field form-map)
       (common/render-help field form-map)]
      [:p {:key (str "li-" (:id field))}
       (common/render-label field form-map)
       (common/render-field (dissoc field :label) form-map)
       (common/render-error-element field form-map)
       (common/render-text field form-map)
       (common/render-help field form-map)])))

(defn as-paragraph
  [& args]
  (let [{:keys [params form-map content]} (util/conform! ::args args)
        {:keys [id]
         :or   {id   (util/gen-id)}} params
        body (common/get-body paragraph params form-map)
        re-render? (helpers/re-render? form-map)]
    (fn [& args]
      (let [{:keys [params form-map]} (util/conform! ::args args)
            {:keys [style
                    class]
             :or {style {}
                  class ""}} params
            body (if re-render? (common/get-body paragraph params form-map) body)]
        [:div {:key (util/slug "form-paragraph" id)
               :style style
               :class class}
         body
         (if content
           [content])]))))
