(ns ui.element.breadcrumbs.views
  (:require [clojure.string :as str]
            [ui.element.breadcrumbs.helpers :refer [crumb]]
            [ui.element.breadcrumbs.spec :as spec]
            [ui.util :as util]))


(defn breadcrumbs
  [& args]
  (let [{:keys [params]} (util/conform! ::spec/args args)
        {:keys [id model]
         :or   {id (util/gen-id)}} params]
    (fn [& args]
      (let [-model (util/deref-or-value model)
            {:keys [crumbs]} (crumb -model {:id id})]
        [:div.Breadcrumbs {:key (util/slug "breadcrumbs" id)}
         crumbs]))))
