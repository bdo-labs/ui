(ns ui.wire.form.wire
  (:require [clojure.spec.alpha :as spec]
            [ui.wire.form.common :as common]
            [ui.wire.wiring :as wiring]
            [ui.util :as util]))


(spec/def ::form map?)
(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))
(spec/def ::wiring vector?)

(spec/def ::wire-params
  (spec/keys :opt-un [::id]
             :req-un [::wiring]))

(spec/def ::wire-args (spec/cat :params ::wire-params :form-map ::form))


(defn- ->kw [name k]
  (keyword (str "$" (clojure.core/name name) "." (clojure.core/name k))))

(defn- assemble-body [{:keys [wiring]} {:keys [fields] :as form-map}]
  (wiring/wiring wiring (reduce (fn [out [_ {:keys [name] :as field}]]
                                  (merge out
                                         {(->kw name :wrapper) wiring/unwrapper
                                          (->kw name :key)     {:key (str "ui-wire-form-wire" (:id field))}
                                          (->kw name :label)   (common/render-label field form-map)
                                          (->kw name :field)   (common/render-field field form-map)
                                          (->kw name :errors)  (common/render-error-element field form-map)
                                          (->kw name :text)    (common/render-text field form-map)
                                          (->kw name :help)    (common/render-help field form-map)}))
                                {} fields)))

(defn as-wire [& args]
  (let [{:keys [params form-map]} (util/conform! ::wire-args args)
        {:keys [id]
         :or   {id (util/gen-id)}} params
        body   (assemble-body params form-map)]
    (fn [& args]
      (let [{:keys [params]} (util/conform! ::wire-args args)
            {:keys [style
                    class]
             :or {style {}
                  class ""}} params]
        [:div {:key (util/slug "form-wire" id)
               :style style
               :class class}
         body]))))
