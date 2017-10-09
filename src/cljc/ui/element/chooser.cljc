(ns ui.element.chooser
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [re-frame.core :as re-frame]
            [clojure.core.async :refer [<! timeout #?(:clj go)]]
            [clojure.spec :as spec]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]
            [ui.element.textfield :refer [textfield]]
            [ui.element.collection :refer [collection]]
            [ui.element.menu :refer [dropdown]]
            [ui.util :as util]
            [clojure.set :as set]))


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
                                  ::searchable
                                  ::predicate?])
              :ui.element.collection/params
              :ui.element.textfield/params))


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
  (let [{:keys [params]}            (util/conform-or-fail ::args args)
        {:keys [id]
         :or {id (util/gen-id)}} params
        query* ^{:doc "Query to use for filtering and emphasizing the resultset"} (atom "")
        show* ^{:doc "Show or hide the collection-dropdown"} (atom false)
        selected* ^{:doc "Keep track of all selected items"} (atom #{})
        textfield-params            (select-keys params (util/keys-from-spec :ui.element.textfield/params))
        collection-params           (select-keys params (util/keys-from-spec :ui.element.collection/params))]

    (letfn [#_(on-key-down
              [event]
              (let [key (util/code->key (.-which event))]
                (when (= "esc" key)
                  (reset! show* false))))]

      #_(when keyboard #?(:cljs (.addEventListener js/document "keydown" on-key-down)))

      (fn [& args]
        (let [{:keys [params]}         (spec/conform ::args args)
              {:keys [items
                      close-on-select
                      multiple
                      label
                      searchable
                      deselectable
                      keyboard
                      predicate?
                      on-change
                      on-focus
                      on-blur
                      on-select]
               :or   {predicate?   str/includes?
                      deselectable true
                      label        ""}} params
              filtered-items           (set/select (labels-by-predicate predicate? @query*) items)
              textfield-params         (merge textfield-params
                                              (when searchable {:label     label
                                                                :on-change (fn [event]
                                                                             (reset! query* (.-value (.-target event)))
                                                                             (when (fn? on-change) (on-change event)))})
                                              (when-not searchable {:label       ""
                                                                    :placeholder label
                                                                    :class       "read-only"
                                                                    :read-only   true})
                                              (when (and (false? multiple)
                                                         (not-empty @selected*)) {:placeholder (-> (first @selected*) :value)})
                                              {:id       (str "textfield-" id)
                                               :value    @query*
                                               :on-focus #(do (reset! show* true)
                                                              (when (fn? on-focus) (on-focus %)))
                                               :on-blur  #(go (<! (timeout 200))
                                                              (when @show*
                                                                (reset! show* false)
                                                                (when (fn? on-blur) (on-blur %))))})
              collection-params        (merge collection-params
                                              {:emphasize    @query*
                                               :deselectable deselectable
                                               :on-select    (fn [items]
                                                               (when close-on-select (reset! show* false))
                                                               (reset! query* "")
                                                               (reset! selected* items)
                                                               (when (fn? on-select) (on-select items)))})]

          [:div.Chooser {:id id}
           [textfield textfield-params]
           [dropdown {:open?  @show*
                      :origin [:top :left]}
            [collection collection-params
             filtered-items]]])))))

