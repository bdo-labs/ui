(ns ui.element.checkbox.views
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [ui.util :as util]
            [ui.element.checkbox.spec :as spec]))

(defn checkbox [& args]
  (let [{:keys [params]} (second (util/conform! ::spec/args args))
        {:keys [model on-change id]
         :or   {id (util/gen-id)}} params
        initial-value (util/deref-or-value model)
        model (if (util/deref? model)
                model
                (atom (cond (keyword? initial-value) initial-value
                            (true? initial-value) :checked
                            :else :not-checked)))
        --on-change #(let [model-value @model]
                       (when (= model-value :checked)
                         (reset! model :not-checked))
                       (when (not= model-value :checked)
                         (reset! model :checked))
                       (when (ifn? on-change) (on-change model-value %)))]
    (fn [& args]
      (let [{:keys [params label]
             :or   {label ""}}      (util/conform! ::spec/args args)
            checked @model]
        [:label {:for   id
                 :class (->> (util/names->str [(case checked
                                                 :checked :Checked
                                                 :indeterminate  :Indeterminate
                                                 :Not-Checked)
                                               (:class params)])
                             (str (when-not (some #(= :Toggle %) (:class params)) " Checkbox ")))}
         [:div.Shape
          [:i (when-not (some #(= :Toggle %) (:class params))
                (case checked
                  :checked {:class :ion-ios-checkmark-empty}
                  :indeterminate  {:class :ion-ios-minus-empty}
                  {}))]
          [:input (merge (dissoc params :model :id :class)
                         {:id      id
                          :type    :checkbox
                          :on-change --on-change
                          :checked checked})]] label]))))
