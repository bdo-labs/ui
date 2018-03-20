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
(spec/def ::deletable boolean?)

(spec/def ::params
  (spec/merge :ui.element.collection/params
              :ui.element.textfield/--params
              (spec/keys :opt-un [::close-on-select
                                  ::deletable
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
        {:keys [id selected deletable
                on-key-up on-change on-focus on-blur on-select]
         :or   {id       (util/gen-id)
                selected #{}}} params
        id                     (util/slug id)
        query*                 ^{:doc "Query to use for filtering and emphasizing the resultset"} (atom (if deletable (str/join ", " (map :value selected)) ""))
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
                    style]
             :or   {predicate?   str/includes?
                    labels       false
                    deselectable true
                    label        ""
                    style        {}}} params
            current-query             (str/trim (last (str/split @query* ",")))
            filtered-items            (set/select (labels-by-predicate predicate? current-query) items)
            textfield-params          (merge params
                                             {:id       (util/slug id "textfield")
                                              :value    @query*
                                              ;; Remove incomplete items
                                              :on-key-up #(let [candidates (->> (str/split (.-value (.-target %)) ",") (mapv str/trim) (set))]
                                                            (reset! selected* (remove (fn [x] (not (contains? candidates (str (:value x))))) @selected*))
                                                            (when (fn? on-key-up) (on-key-up %)))
                                              :on-focus #(do (reset! show* true)
                                                             (when (fn? on-focus) (on-focus %)))
                                              :on-blur  #(go (<! (timeout 160))
                                                             (when @show* (reset! show* false))
                                                             (when (fn? on-blur) (on-blur %)))}
                                             (if searchable
                                               {:label     label
                                                :on-change #(do (reset! query* (.-value (.-target %)))
                                                                (when (fn? on-change) (on-change %)))}
                                               {:label       ""
                                                :placeholder label
                                                :class       "read-only"
                                                :read-only   true})
                                             (when (and (not deletable)
                                                        (false? multiple)
                                                        (not-empty @selected*))
                                               {:placeholder (-> (first @selected*) :value)})
                                             (when (and (not deletable)
                                                        multiple
                                                        (false? labels)
                                                        (not (empty? @selected*)))
                                               {:placeholder (str (str/join ", " (map :value @selected*)))}))
            collection-params         (merge params
                                             {:id           (util/slug id "collection")
                                              :emphasize    current-query
                                              :selected     @selected*
                                              :deselectable deselectable
                                              :on-select    (fn [items]
                                                              (when (true? close-on-select) (reset! show* false))
                                                              (if deletable
                                                                (reset! query* (str (str/join ", " (map :value items)) (when multiple ", ")))
                                                                (reset! query* ""))
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
