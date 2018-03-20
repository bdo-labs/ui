(ns ui.element.label.views
  (:require [ui.element.label.spec :as spec]
            [ui.util :as util]))

(defn label
  [& args]
  (let [{:keys [params]}               (util/conform! ::spec/args args)
        {:keys [id value on-key-down]} params
        id                             (util/slug id value "label")]
    [:span.Label {:key (util/gen-id)}
     [:input {:id id :type :text :on-key-down on-key-down}]
     [:label {:for id} value]]))

