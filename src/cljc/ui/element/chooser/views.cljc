(ns ui.element.chooser.views
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.core.async :refer [<! timeout #?(:clj go)]]
            [clojure.string :as str]
            [ui.element.textfield.views :refer [textfield]]
            [ui.element.collection.views :refer [collection]]
            [ui.element.menu.views :refer [dropdown]]
            [ui.element.badge.views :refer [badge]]
            [ui.element.label.views :as l]
            [ui.element.chooser.spec :as spec]
            [ui.util :as util]
            [clojure.set :as set]))

;; Helper functions -------------------------------------------------------

(defn- labels-by-predicate [predicate? query]
  (fn [item]
    (let [label (or (:label item)
                    (:value item))]
      (predicate? label query))))

;; Views ------------------------------------------------------------------

(defn chooser
  [& args]
  (let [{:keys [params]}       (util/conform! ::spec/args args)
        {:keys [id model deletable items
                on-key-up on-change on-focus on-blur on-select]
         :or   {id       (util/gen-id)
                model #{}}} params
        id                     (util/slug "chooser" id)
        query*                 ^{:doc "Query to use for filtering and emphasizing the resultset"} (atom (if deletable (str/join ", " (map :value model)) ""))
        show*                  ^{:doc "Show or hide the collection-dropdown"} (atom false)
        model                  ^{:doc "Keep track of all selected items"} (cond (util/deref? model) model
                                                                                (set? model) (atom model)
                                                                                :else (atom #{}))]
    (fn [& args]
      (let [{:keys [params]}          (util/conform! ::spec/args args)
            {:keys [multiple
                    labels
                    label
                    searchable
                    selectable
                    deselectable
                    keyboard
                    predicate?
                    style]
             :or   {predicate?   str/includes?
                    selectable   true
                    searchable   true
                    keyboard     true
                    labels       false
                    deselectable true
                    label        ""
                    style        {}}} params
            current-query             (str/trim (last (str/split @query* ",")))
            filtered-items            (set/select (labels-by-predicate predicate? current-query) items)
            textfield-params          (merge params
                                             {:id        (util/slug id "textfield")
                                              :model     query*
                                              ;; Remove incomplete items
                                              :on-key-up (fn [key value event]
                                                           (let [candidates (->> (str/split value ",") (mapv str/trim) (set))
                                                                 valid-ones (remove (fn [x] (not (contains? candidates (str (:value x))))) @model)]
                                                             (when (not= @model valid-ones)
                                                               (reset! model (set valid-ones))))
                                                           (when (fn? on-key-up) (on-key-up key value event)))
                                              :on-focus  (fn [value event]
                                                           (do (reset! show* true)
                                                               (when (fn? on-focus) (on-focus value event))))
                                              :on-blur   (fn [value event]
                                                           (do (.persist event)
                                                               (go (<! (timeout 160))
                                                                   (when @show* (reset! show* false))
                                                                   (when (ifn? on-blur) (on-blur value event)))))}
                                             (if searchable
                                               {:label     label
                                                :on-change #(when (ifn? on-change) (on-change %))}
                                               {:label       ""
                                                :placeholder label
                                                :class       "read-only"
                                                :read-only   true})
                                             (when (and (not deletable)
                                                        (false? multiple)
                                                        (not-empty @model))
                                               {:placeholder (-> (first @model) :value)})
                                             (when (and (not deletable)
                                                        multiple
                                                        (false? labels)
                                                        (not (empty? @model)))
                                               {:placeholder (str (str/join ", " (map :value @model)))}))
            collection-params         (merge params
                                             {:id           (util/slug id "collection")
                                              :emphasize    current-query
                                              :model        model
                                              :keyboard     keyboard
                                              :selectable   selectable
                                              :searchable   searchable
                                              :deselectable deselectable
                                              :on-select    (fn [items]
                                                              (if deletable
                                                                (reset! query* (str (str/join ", " (map :value items)) (when multiple ", ")))
                                                                (reset! query* ""))
                                                              (reset! model items)
                                                              (when (fn? on-select) (on-select items)))})]

        [:div.Chooser {:id    id
                       :key   (util/slug id "key")
                       :style style}
         [textfield textfield-params]
         [dropdown {:open? @show*
                    :origin [:top :left]}
          [collection collection-params filtered-items]]

         (when (and multiple
                    (false? labels))
           [badge {:show-content? true} (count @model)])

         (when (and multiple labels)
           [:div.Labels
            (doall
             (for [label-params model]
               (let [label-params (merge label-params
                                         {:key         (util/slug (:id label-params) "a-label")
                                          :on-key-down #(let [key (util/code->key (.-which %))]
                                                          (case key
                                                            ("backspace" "delete") (util/log "Remove " (:value label-params))))})]
                 [l/label label-params])))])]))))
