(ns ui.element.chooser
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.core.async :refer [<! timeout #?(:clj go)]]
            [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]
            [ui.element.textfield :refer [textfield]]
            [ui.element.collection :refer [collection]]
            [ui.element.menu :refer [dropdown]]
            [ui.element.badge :refer [badge]]
            [ui.element.label :as l]
            [ui.util :as util]
            [clojure.set :as set]))


;; Specification ----------------------------------------------------------


(spec/def ::maybe-fn
  (spec/with-gen fn?
    (gen/return (constantly nil))))


(spec/def ::close-on-select boolean?)
(spec/def ::labels boolean?)
(spec/def ::searchable boolean?)
(spec/def ::predicate? ::maybe-fn)

(spec/def ::params
  (spec/merge :ui.element.collection/params
              :ui.element.textfield/--params
              (spec/keys :opt-un [::close-on-select
                                  ::searchable
                                  ::labels
                                  ::predicate?])))


(spec/def ::args
  (spec/cat :params ::params))


;; Helper functions -------------------------------------------------------


(defn- labels-by-predicate [predicate? query]
  (fn [item]
    (let [label (or (:label item)
                    (:value item))]
      (predicate? label query))))


;; Views ------------------------------------------------------------------


(defn chooser
  [& args]
  (let [{:keys [params]}       (util/conform! ::args args)
        {:keys [id selected]
         :or   {id       (util/gen-id)
                selected #{}}} params
        id                     (util/slug id)
        query*                 ^{:doc "Query to use for filtering and emphasizing the resultset"} (atom "")
        show*                  ^{:doc "Show or hide the collection-dropdown"} (atom false)
        selected*              ^{:doc "Keep track of all selected items"} (atom selected)
        collection-params      {:selected selected}]

    (fn [& args]
      (let [{:keys [params]}          (spec/conform ::args args)
            {:keys [items
                    close-on-select
                    multiple
                    labels
                    label
                    searchable
                    deselectable
                    keyboard
                    predicate?
                    on-change
                    on-focus
                    on-blur
                    on-select
                    style]
             :or   {predicate?   str/includes?
                    labels       false
                    deselectable true
                    label        ""
                    style        {}}} params
            filtered-items            (set/select (labels-by-predicate predicate? @query*) items)
            textfield-params          (merge params
                                             {:id       (util/slug id "textfield")
                                              :value    @query*
                                              :on-focus #(do (reset! show* true)
                                                             (when (fn? on-focus) (on-focus %)))
                                              :on-blur  #(do (when (fn? on-blur) (on-blur %))
                                                             (go (<! (timeout 200))
                                                                 (when @show* (reset! show* false))))}
                                             (if searchable
                                               {:label     label
                                                :on-change (fn [event]
                                                             (reset! query* (.-value (.-target event)))
                                                             (when (fn? on-change) (on-change event)))}
                                               {:label       ""
                                                :placeholder label
                                                :class       "read-only"
                                                :read-only   true})
                                             (when (and (false? multiple)
                                                        (not-empty @selected*))
                                               {:placeholder (-> (first @selected*) :value)})
                                             (when (and multiple
                                                        (false? labels)
                                                        (not (empty? @selected*)))
                                               {:placeholder (str/join ", " (map :value @selected*))}))
            collection-params         (merge params
                                             {:id           (util/slug id "collection")
                                              :emphasize    @query*
                                              :selected     @selected*
                                              :deselectable deselectable
                                              :on-select    (fn [items]
                                                              (when (true? close-on-select) (reset! show* false))
                                                              (reset! query* "")
                                                              (reset! selected* items)
                                                              (when (fn? on-select) (on-select items)))})]

        [:div.Chooser {:id    id
                       :key   (util/slug id "key")
                       :style style}
         [textfield textfield-params]
         [dropdown {:open? @show*}
          [collection collection-params
           filtered-items]]

         (when (and multiple
                    (false? labels))
           [badge {:show-count true} (count @selected*)])

         (when (and multiple labels)
           [:div.Labels
            (doall
             (for [label-params @selected*]
               (let [label-params (merge label-params
                                         {:key         (util/slug (:id label-params) "a-label")
                                          :on-key-down #(let [key (util/code->key (.-which %))]
                                                          (case key
                                                            ("backspace" "delete") (util/log "Remove " (:value label-params))))})]
                 [l/label label-params])))])]))))

