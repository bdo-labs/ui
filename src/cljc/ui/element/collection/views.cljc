(ns ui.element.collection.views
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            #?(:cljs [reagent.core :as reagent])
            [clojure.string :as str]
            [clojure.set :as set]
            [ui.element.collection.spec :as spec]
            [ui.util :as util]))

;; Helper functions -------------------------------------------------------

(defn- next-item
  "Return the next item from collection"
  [coll item]
  (if (and (some? item)
           (contains? coll item))
    (loop [[curr & tail] coll]
      (if (= item curr)
        (if (nil? (first tail))
          curr
          (first tail))
        (recur tail)))
    (first coll)))

(defn- prev-item
  "Return the previous item from collection"
  [coll item]
  (if (and (some? item)
           (contains? coll item))
    (loop [prev          (first coll)
           [curr & tail] (rest coll)]
      (if (or (= item curr)
              (= item prev))
        prev
        (recur curr tail)))
    (first coll)))

;; View -------------------------------------------------------------------

(defn- emphasize-match
  "Emphasize the [substr]-match within [s]tring"
  [s substr]
  (if (or (empty? s) (empty? substr))
    s
    (let [pattern (re-pattern (str "(?i)" substr))]
      (->> (-> (str/replace s pattern #(str ":" %1 ":"))
               (str/split #":"))
           (map-indexed #(if (util/=i %2 substr)
                           [:strong {:key (str "i-" %2 "-" %1 "-emp")} %2]
                           [:span {:key (str "i-" %2 "-" %1 "-no")} %2]))))))

;; FIXME These needs to be part of the component
;; The component re-runs all life-cycle-events upon each change, why?
(def intended* (atom nil))
(def selected* (atom (sorted-map)))

;; TODO Scroll list upon navigating using keyboard
;; TODO Move keyboard-events out of the component
;; TODO Multiple selection using ctrl/shift
;; TODO Allow custom markup in each item?
#?(:clj (defn collection [] [:span "Not implemented"])
   :cljs
   (defn collection
     [& args]
     (let [;; intended*                  ^{:doc "Items that are intermediately marked for selection"} (atom nil)
           ;; selected*                  ^{:doc "A sorted-map of sets of items that's been selected"} (atom (sorted-map))
           {:keys [params items]}     (util/conform! ::spec/args args)
           {:keys [id
                   deselectable
                   multiple
                   selected
                   keyboard
                   on-select
                   max-items
                   multiple
                   predicate?
                   add-message
                   empty-message
                   hide-selected]
            :or   {id            (util/gen-id)
                   deselectable  true
                   selected      #{}
                   keyboard      true
                   predicate?    str/includes?
                   add-message   false
                   empty-message false
                   hide-selected false}} params
           id                         (util/slug id)
           items                      (apply sorted-set-by (fn [a b] (< (:value a) (:value b))) items)
           ui-params                  (select-keys params (util/keys-from-spec ::spec/params))]

       ;; State management
       (letfn [(set-intended! [item]
                 (reset! intended* item))
               (remove-item! [item]
                             (swap! selected* disj item))
               (add-item! [item]
                          (when-not (empty? item)
                            (if (contains? selected item)
                              (when deselectable
                                (if (= 1 (count selected))
                                  (remove-item! item)
                                  (reset! selected* (set/difference selected #{item}))))
                              (when (or (nil? max-items)
                                        (> max-items (count selected)))
                                (if (or (not multiple)
                                        (empty? selected))
                                  (reset! selected* #{item})
                                  (swap! selected* conj item))))
                            (when (fn? on-select)
                              (on-select @selected*))))
               (on-key-down [event]
                            (let [key (util/code->key (.-which event))]
                              (case key
                                "up"    (set-intended! (prev-item items @intended*))
                                "down"  (set-intended! (next-item items @intended*))
                                "enter" (add-item! @intended*)
                                true)))]

         (reagent/create-class
          {:display-name           "collection"
           ;; We ensure that the collection is initialized with whatever
           ;; selection was passed in
           :component-did-mount    #(do (reset! selected* selected)
                                        (when keyboard (.addEventListener js/document "keydown" on-key-down)))
           :component-will-unmount #(.removeEventListener js/document "keydown" on-key-down)
           :reagent-render
           (fn [& args]
             (let [{:keys [params]}    (util/conform! ::spec/args args)
                   {:keys [emphasize]} params
                   selected            @selected*]
               [:ul.Collection {:key   (util/slug id "collection")
                                :id    id
                                :class (util/params->classes ui-params)}
                ;; Add new item
                (when (and (not (empty? emphasize))
                           (predicate? emphasize emphasize)
                           (not (false? add-message))
                           (not= emphasize (:value (first items))))
                  (let [item {:id nil :value emphasize}]
                    [:li {:key            (util/slug id "adder")
                          :class          (str/join " " [(when (= item @intended*) "intended")
                                                         (when (contains? selected item) "selected")])
                          :on-mouse-enter #(set-intended! item)
                          :on-click       #(add-item! item)}
                     (emphasize-match (str/replace-first add-message "%" emphasize) emphasize)]))
                ;; No results
                (when (and (empty? items)
                           (predicate? emphasize emphasize)
                           (not (false? empty-message)))
                  [:li {:key (util/slug id "empty")}
                   (emphasize-match (str/replace-first empty-message "%" emphasize) emphasize)])
                ;; List items
                (when-not (empty? items)
                  (doall
                   (for [{:keys [value label]
                          :or   {label value}
                          :as   item} items]
                     (when (or (false? hide-selected)
                               (not (contains? selected item)))
                       [:li {:key            (util/gen-id)
                             ;; (util/slug id (:id item) "listitem")
                             :class          (str/join " " [(when (and (= item @intended*)
                                                                       (or (nil? max-items)
                                                                           (< (count selected) max-items))) "intended")
                                                            (when (contains? selected item) "selected")
                                                            (when (and (not (contains? selected item))
                                                                       (and (not (nil? max-items))
                                                                            (>= (count selected) max-items))) "readonly")])
                             :on-mouse-enter #(set-intended! item)
                             :on-click       #(add-item! item)}
                        (emphasize-match label emphasize)]))))]))})))))
