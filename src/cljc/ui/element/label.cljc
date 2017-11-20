(ns ui.element.label
  (:require [clojure.spec.alpha :as spec]
            [ui.util :as util]))


(spec/def ::params
  (spec/keys :req-un [::value]
             :opt-un [::id ::on-key-down]))


(spec/def ::args
  (spec/cat :params ::params))


(defn label
  [& args]
  (let [{:keys [params]}               (util/conform-or-fail ::args args)
        {:keys [id value on-key-down]} params
        id                             (util/slug id value "label")]
    [:span.Label {:key (util/gen-id)}
     [:input {:id id :type :text :on-key-down on-key-down}]
     [:label {:for id} value]]))

