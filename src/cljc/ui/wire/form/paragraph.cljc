(ns ui.wire.form.paragraph
  (:require [clojure.spec.alpha :as spec]
            [ui.wire.form.common :as common]
            [ui.util :as util]))


(spec/def ::form map?)
(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))
(spec/def ::type #{:ul :ol})


(spec/def ::paragraph-params
  (spec/keys :opt-un [::id
                      ::common/wiring
                      ::type]))

(spec/def ::paragraph-args (spec/cat :params ::paragraph-params :form-map ::form))


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
  (let [{:keys [params form-map]} (util/conform! ::paragraph-args args)
        {:keys [id]
         :or   {id   (util/gen-id)}} params
        body (common/get-body paragraph params form-map)]
    (fn [& args]
      (let [{:keys [params]} (util/conform! ::paragraph-args args)
            {:keys [style
                    class]
             :or {style {}
                  class ""}} params]
        [:div {:key (util/slug "form-paragraph" id)
               :style style
               :class class}
         body]))))
