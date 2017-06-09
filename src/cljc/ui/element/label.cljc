(ns ui.element.label
  (:require [clojure.spec :as spec]))


(spec/def ::label-params
  (spec/keys :req-un [::text]
             :opt-un [::id ::on-key-down]))


(spec/def ::label-args
  (spec/cat :params ::label-params))


(defn label
  [{:keys [id text on-key-down]}]
  (let [uid (str "label-" id)]
    (fn []
      [:span.Label
       [:input {:id uid :type :text :on-key-down on-key-down}]
       [:label {:for uid} text]])))


(spec/fdef label
        :args ::label-args
        :ret vector?)
