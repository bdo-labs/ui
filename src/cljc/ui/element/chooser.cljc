(ns ui.element.chooser
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec :as spec]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]
            [ui.element.textfield :refer [textfield]]
            [ui.element.collection :refer [collection]]
            [ui.util :as util]))


;; Specification ----------------------------------------------------------


(spec/def ::maybe-fn
  (spec/with-gen fn?
    (gen/return (constantly nil))))


(spec/def ::close-on-select boolean?)
(spec/def ::multiple boolean?)
(spec/def ::searchable boolean?)
(spec/def ::predicate? ::maybe-fn)


(spec/def ::params
  (spec/merge (spec/keys :opt-un [::close-on-select
                                  ::multiple
                                  ::searchable
                                  ::predicate?])
              :ui.element.collection/params
              :ui.element.textfield/params))


(spec/def ::args
  (spec/cat :params ::params))


(defn labels-by-predicate [predicate? query]
  (fn [item]
    (let [label (or (:label item)
                    (:value item))]
      (predicate? label query))))


;; Views ------------------------------------------------------------------


(defn chooser
  [& args]
  (let [{:keys [params]}                   (util/conform-or-fail ::args args)
        {:keys [id
                items
                value
                on-change
                searchable
                predicate?]
         :or   {predicate? str/includes?}} params
        query*                             (atom (or value ""))
        textfield-params                   (merge (select-keys params (util/keys-from-spec :ui.element.textfield/params))
                                                  (when searchable {:on-change #(do (reset! query* (.-value (.-target %)))
                                                                                    (when (fn? on-change) (on-change %)))}))]
    (fn []
      (let [filtered-items   (filter (labels-by-predicate predicate? @query*) items)
            textfield-params (merge textfield-params
                                    (when (not-empty @query*)
                                      {:class (str "dirty " (:class textfield-params))}))]
        [:div.Chooser {:key (str "chooser-" id)}
         [textfield textfield-params]
         [collection filtered-items]]))))
