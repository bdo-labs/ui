(ns ui.wire.form.template
  (:require [clojure.spec.alpha :as spec]
            [ui.specs :as specs.common]
            [ui.wire.form.common :as common]
            [ui.wire.form.helpers :as helpers]
            [ui.util :as util]))


(spec/def ::params
  (spec/keys :opt-un [::specs.common/id
                      ::common/wiring]
             :req-un [::common/template]))


(spec/def ::args (spec/cat :params ::params :form-map ::common/form :content (spec/? ::common/content)))


(defn row [{:keys [wiring]} _]
  wiring)

(defn- adapt-wiring [{:keys [template] :as params} form-map]
  (assoc params :wiring
         (reduce (fn [out [_ {:keys [name]}]]
                   (assoc out name template))
                 {} (:fields form-map))))

(defn as-template [& args]
  (let [{:keys [params form-map content]} (util/conform! ::args args)
        {:keys [id template]
         :or   {id (util/gen-id)}} params
        body (common/get-body row (adapt-wiring params form-map) form-map)
        re-render? (helpers/re-render? form-map)]
    (fn [& args]
      (let [{:keys [params form-map]} (util/conform! ::args args)
            {:keys [style
                    class]
             :or {style {}
                  class ""}} params
            body (if re-render? (common/get-body row (adapt-wiring params form-map) form-map) body)]
        [:div {:key (util/slug "form-template" id)
               :style style
               :class class}
         body
         (if content
           [content])]))))
