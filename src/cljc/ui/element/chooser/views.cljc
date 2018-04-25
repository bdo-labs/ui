(ns ui.element.chooser.views
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.core.async :refer [<! timeout #?(:clj go)]]
            [clojure.string :as str]
            [ui.element.textfield.views :refer [textfield]]
            [ui.element.textfield.spec :as textfield-spec]
            [ui.element.collection.views :refer [collection]]
            [ui.element.collection.spec :as collection-spec]
            [ui.element.menu.views :refer [dropdown]]
            [ui.element.badge.views :refer [badge]]
            [ui.element.label.views :as l]
            [ui.element.chooser.spec :as spec]
            [ui.util :as util]))

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
        {:keys [id selected deletable items multiple
                on-key-up on-change on-focus on-blur on-select]
         :or   {id       (util/gen-id)
                selected []}} params
        id                     (util/slug id)
        query*                 ^{:doc "Query to use for filtering and emphasizing the resultset"} (atom (if deletable (str (str/join ",\u2002" (map :value selected)) (when (and (seq selected) multiple) ",\u2002")) ""))
        focus*                 ^{:doc "We need to keep track of focus due to mounting/un-mounting collection"} (atom false)
        show*                  ^{:doc "Show or hide the collection-dropdown"} (atom false)
        selected*              ^{:doc "Keep track of all selected items"} (atom selected)]

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
                    deselectable true
                    labels       false
                    label        ""
                    style        {}}} params

            current-query             (if (= (last @query*) \u2002) "" (str/trim (last (str/split @query* ",\u2002"))))
            filtered-items            (filter (fn [{:keys [value]}] (predicate? value current-query)) items)

            textfield-params          (merge (dissoc params :items)
                                             {:id        (util/slug id "textfield")
                                              :model     query*
                                              ;; Remove incomplete items
                                              :on-key-up (fn [key value event]
                                                           (when (and (or (= key "delete")
                                                                          (= key "backspace")) (= (last value) ","))
                                                             (let [el       (.-target event)
                                                                   to       (count value)
                                                                   last-sep (str/last-index-of value ",\u2002")
                                                                   from     (if (nat-int? last-sep) (+ 2 last-sep) 0)]
                                                               (.setSelectionRange el from to)))
                                                           (let [candidates (->> (str/split value ",\u2002") (mapv str/trim))
                                                                 valid-ones (remove (fn [{:keys [value]}] (not (nat-int? (.indexOf candidates value)))) @selected*)]
                                                             (when (not= (count @selected*) (count valid-ones))
                                                               (reset! selected* valid-ones)))
                                                           (when (fn? on-key-up) (on-key-up key value event)))
                                              ;; Reveal collection and execute external on-focus
                                              :on-focus  (fn [value event]
                                                           (let [el    (.-target event)
                                                                 value (.-value el)]
                                                             (.setSelectionRange el (dec (count value)) (count value))
                                                             (set! (.-scrollLeft el) (.-scrollWidth el))
                                                             (reset! focus* true)
                                                             (go (<! (timeout 20))
                                                                 (do (reset! show* true)
                                                                     (when (fn? on-focus) (on-focus value event))))))
                                              ;; Hide collection and execute external on-blur
                                              :on-blur   (fn [value event]
                                                           (do (when @focus* (reset! focus* false))
                                                               (when @show* (reset! show* false))
                                                               (when (ifn? on-blur) (on-blur value event))))}
                                             (if searchable
                                               {:label     label
                                                :on-change #(when (ifn? on-change) (on-change %))}
                                               {:label       ""
                                                :placeholder label
                                                :class       "read-only"
                                                :read-only   true})
                                             ;; When free-editing is disabled
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
                                              :model        selected*
                                              :keyboard     keyboard
                                              :selectable   selectable
                                              :searchable   searchable
                                              :deselectable deselectable
                                              :on-select    (fn [items]
                                                              (if deletable
                                                                (reset! query* (str (str/join ",\u2002" (map :value items))
                                                                                    (when (and (seq items) multiple) ",\u2002")))
                                                                (reset! query* ""))
                                                              (reset! selected* items)
                                                              (when (fn? on-select) (on-select items)))})]

        [:div.Chooser {:id id :key (util/slug id "key") :style style}
         [textfield textfield-params]
         (when @focus*
           [dropdown {:open? @show* :origin [:top :left]}
            [collection collection-params filtered-items]])

         (when (and multiple (false? labels))
           [badge {:show-content? true} (count @selected*)])

         (when (and multiple labels)
           [:div.Labels
            (doall
             (for [label-params @selected*]
               (let [on-key-down  #(let [key (util/code->key (.-which %))]
                                     (case key
                                       ("backspace" "delete")
                                       (util/log "Remove " (:value label-params))))
                     label-params (merge label-params
                                         {:key         (util/slug (:id label-params) "a-label")
                                          :on-key-down on-key-down})]
                 [l/label label-params])))])]))))
