(ns ui.element.checkbox
  (:require [ui.util :as u]
            [clojure.spec.alpha :as spec]))



(spec/def ::id (spec/or :numeric int?
                        :textual (spec/and string? not-empty)))
(spec/def ::checked? boolean?)
(spec/def ::label string?)


(spec/def ::checkbox-params
  (spec/keys
   :req-un [::checked?]
   :opt-un [::id]))


(spec/def ::checkbox-args
  (spec/cat :params ::checkbox-params
            :label ::label))


(defn conform-or-fail [spec args]
  (if (spec/valid? spec args)
    (spec/conform spec args)
    (u/exception (spec/explain-str spec args))))



(defn checkbox
  [& args]
  (let [{:keys [params label]
         :or   {label ""}}        (conform-or-fail ::checkbox-args args)
        {:keys [checked? on-click id]
         :or   {id (u/gen-id)}} params]
    (u/log (some #(= :Toggle %) [:Toggle]))
    [:label {:for   id
             :class (u/names->str [(when-not (some #(= :Toggle %) (:class params)) :Checkbox)
                                   (when checked? :Checked)
                                   (:class params)])}
     [:div.Shape
      [:i (when-not (some #(= :Toggle %) (:class params)) {:class :ion-ios-checkmark-empty})]
      [:input (merge (dissoc params :checked? :id :class)
                     {:id      id
                      :type    :checkbox
                      :checked checked?})]] label]))


(spec/fdef checkbox
        :args ::checkbox-args
        :ret vector?)
