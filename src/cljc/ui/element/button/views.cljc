(ns ui.element.button.views
  (:require [ui.element.button.spec :as spec]
            [ui.util :as util]))

(defn button
  "Takes an optional `map` of parameters and arbitrary content."
  [& args]
  (let [{:keys [params content]} (util/conform! ::spec/args args)
        class                    (util/params->classes params)
        ui-params                (conj (util/keys-from-spec ::spec/params) :class)
        params                   (->> (apply dissoc params ui-params)
                                      (merge {:class class}))]
    (into [:button.Button params] content)))
