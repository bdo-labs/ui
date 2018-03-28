(ns ui.wire.form.template
  (:require [clojure.spec.alpha :as spec]
            [ui.wire.form.common :as common]
            [ui.wire.wiring :as wiring]
            [ui.util :as util]))


(spec/def ::form map?)
(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))

(spec/def ::template-params
  (spec/keys :opt-un [::id
                       ::common/wiring]
             :req-un [::common/template]))


(spec/def ::template-args (spec/cat :params ::template-params :form-map ::form))


(defn row [{:keys [wiring]} _]
  wiring)

(defn- adapt-wiring [{:keys [template] :as params} form-map]
  (assoc params :wiring
         (reduce (fn [out [_ {:keys [name]}]]
                   (assoc out name template))
                 {} (:fields form-map))))

(defn as-template [& args]
  (let [{:keys [params form-map]} (util/conform! ::template-args args)
        {:keys [id template]
         :or   {id (util/gen-id)}} params
        body (common/get-body row (adapt-wiring params form-map) form-map)]
    (println (adapt-wiring params form-map))
    (fn [& args]
      (let [{:keys [params]} (util/conform! ::template-args args)
            {:keys [style
                    class]
             :or {style {}
                  class ""}} params]
        [:div {:key (util/slug "form-template" id)
               :style style
               :class class}
          body
         ;;"foobar"
         ]))))
