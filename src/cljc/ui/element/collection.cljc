(ns ui.element.collection
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [re-frame.core :as re-frame]
            [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]
            [clojure.set :as set]
            [ui.element.label :refer [label]]
            [ui.util :as util]))


;; Specification ----------------------------------------------------------


(spec/def ::maybe-fn
  (spec/with-gen fn?
    (gen/return (constantly nil))))


(spec/def ::on-click ::maybe-fn)
(spec/def ::on-select ::maybe-fn)
(spec/def ::qualified-string? (spec/and string? not-empty))


;; These are attributes of an item
;; Label will equal value if it's not explicitly set
(spec/def ::id (spec/or :str ::qualified-string? :num nat-int?))
(spec/def ::value ::qualified-string?)
(spec/def ::label ::qualified-string?)
(spec/def ::item
  (spec/nonconforming
   (spec/or
    :without-label (spec/cat :id ::id :value ::value)
    :with-label (spec/cat :id ::id :value ::value :label ::label)
    :qualified (spec/keys :req-un [::id ::value] :opt-un [::label]))))


;; Wether you can select multiple items
(spec/def ::multiple boolean?)


;; Substring to emphasize in the item-labels
(spec/def ::emphasize string?)


;; Makes it possible to not have a selection
(spec/def ::deselectable boolean?)


(spec/def ::hide-selected boolean?)


;; Add free-text item to collection
(spec/def ::add-message ::qualified-string?)


;; Text to display when there's no matches
(spec/def ::empty-message ::qualified-string?)


(spec/def ::max-items nat-int?)


;; Enable keyboard-support
;; ATM, only one collection can have keyboard-support
;; enabled at a time
(spec/def ::keyboard boolean?)


;; The list of items should always be distinct, so you would probably
;; pass it a `set`
(spec/def ::items (spec/coll-of ::item
                                :kind set?))


(spec/def ::params
  (spec/keys :opt-un [::id
                      ::on-click
                      ::on-select
                      ::max-items
                      ::emphasize
                      ::deselectable
                      ::multiple
                      ::keyboard
                      ::add-message
                      ::empty-message
                      ::hide-selected]))


(spec/def ::args
  (spec/cat :params ::params :items ::items))


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



(def selected* ^{:doc "A sorted-map of sets, of items that's been selected"} (atom (sorted-map)))


;; TODO Scroll list upon navigating using keyboard
;; TODO Move keyboard-events out of the component
;; TODO Multiple selection using ctrl/shift
;; TODO Allow hiccup-content in the item?
(defn collection
  [& args]
  (let [intended*                  ^{:doc "Items that are intermediately marked for selection"} (atom nil)
        {:keys [params]}           (util/conform-or-fail ::args args)
        {:keys [id
                multiple]
         :or   {id (util/gen-id)}} params]
    (fn [& args]
      (let [{:keys [params items]}        (util/conform-or-fail ::args args)
            {:keys [emphasize
                    multiple
                    deselectable
                    keyboard
                    add-message
                    empty-message
                    on-select
                    hide-selected
                    max-items]
             :or   {keyboard      true
                    deselectable  true
                    add-message   false
                    empty-message false
                    hide-selected false}} params
            id                            (util/slug id)
            items                         (apply sorted-set-by (fn [a b] (< (:value a) (:value b))) items)
            selected                      (get @selected* id)]

        ;; State management
        (letfn [(set-intended! [item]
                  (reset! intended* item))
                (remove-item! [item]
                  (swap! selected* update-in [id] disj item))
                (add-item! [item]
                  (when-not (empty? item)
                    (if (contains? selected item)
                      (when deselectable (swap! selected* assoc-in [id] (set/difference selected #{item})))
                      (when (or (nil? max-items)
                                (> max-items (count selected)))
                        (if (or (not multiple)
                                (empty? selected))
                          (swap! selected* assoc-in [id] #{item})
                          (swap! selected* update-in [id] conj item))))
                    (when (and (fn? on-select)
                               (not (empty? (get @selected* id))))
                      (on-select (get @selected* id)))))
                (on-key-down [event]
                  (let [key (util/code->key (.-which event))]
                    (case key
                      "up"    (set-intended! (prev-item items @intended*))
                      "down"  (set-intended! (next-item items @intended*))
                      "enter" (add-item! @intended*)
                      true)))]

          #?(:cljs (when keyboard (set! (.-onkeydown js/document) on-key-down)))

          [:ul.Collection {:key (util/slug id "collection")
                           :id  id}

           ;; Add new item
           (when (and (not (empty? emphasize))
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
                   (emphasize-match label emphasize)]))))])))))
