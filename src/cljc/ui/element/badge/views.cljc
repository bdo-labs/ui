(ns ui.element.badge.views
  (:require [ui.element.badge.spec :as spec]
            [ui.util :as util]))

(defn badge [& args]
  (let [{:keys [params content]} (util/conform! ::spec/args args)
        {:keys [show-count class]
         :or   {show-count true
                class      ""}}  params
        ui-params                (util/keys-from-spec ::spec/params)
        class                    (util/params->classes params)]
    (when (some? content)
      [:div.Badge (merge {:class class}
                         (apply dissoc params (conj ui-params :class)))
       (when show-count content)])))
