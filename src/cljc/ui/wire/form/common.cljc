(ns ui.wire.form.common
  #?(:cljs (:require [reagent.core :refer [atom] :as reagent])))


(defn render-field [{:keys [field-fn name] :as field} form-map]
  [field-fn (assoc field :model #?(:cljs (reagent/cursor (:data form-map) [name])
                                   :clj  (get-in form-map [:data name])))])
