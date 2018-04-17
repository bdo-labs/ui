(ns ui.element.badge.views
  (:require [ui.element.badge.spec :as spec]
            [ui.util :as util]
            [clojure.string :as str]))

(defn badge [& args]
  (let [{:keys [params content]} (util/conform! ::spec/args args)
        {:keys [show-content? class]
         :or   {show-content? true
                class      ""}}  params
        ui-params                (util/keys-from-spec ::spec/params)
        class                    (str/join " " [(name (first content)) (util/params->classes params)])]
    (when (some? content)
      [:div.Badge (merge {:class class}
                         (apply dissoc params (conj ui-params :class)))
       (when show-content? (last content))])))
