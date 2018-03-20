(ns ui.element.checkbox.views
  (:require [ui.util :as util]
            [ui.element.checkbox.spec :as spec]))

(defn checkbox
  [& args]
  (let [{:keys [params label]
         :or   {label ""}}      (util/conform! ::spec/checkbox-args args)
        {:keys [checked on-click id]
         :or   {id (util/gen-id)}} params]
    [:label {:for   id
             :class (->> (util/names->str [(case checked
                                             (true :checked) :Checked
                                             :indeterminate  :Indeterminate
                                             :Not-Checked)
                                           (:class params)])
                         (str (when-not (some #(= :Toggle %) (:class params)) " Checkbox ")))}
     [:div.Shape
      [:i (when-not (some #(= :Toggle %) (:class params))
            (case checked
              (true :checked) {:class :ion-ios-checkmark-empty}
              :indeterminate  {:class :ion-ios-minus-empty}
              {}))]
      [:input (merge (dissoc params :checked :id :class)
                     {:id      id
                      :type    :checkbox
                      :checked checked})]] label]))
