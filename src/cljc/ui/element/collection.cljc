(ns ui.element.collection
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [re-frame.core :as re-frame]
            [clojure.spec :as spec]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]
            [clojure.set :as set]
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
  (spec/or
   :without-label (spec/cat :id ::id :value ::value)
   :with-label (spec/cat :id ::id :value ::value :label ::label)
   :qualified (spec/keys :req-un [::id ::value] :opt-un [::label])))


;; Wether you can select multiple items
(spec/def ::multiple boolean?)


;; Substring to emphasize in the item-labels
(spec/def ::emphasize string?)


;; Makes it possible to not have a selection
(spec/def ::deselectable boolean?)


;; Add free-text item to collection
(spec/def ::addable (spec/or :bool boolean?
                             :str ::qualified-string?))


;; Text to display when there's no matches
(spec/def ::empty-message ::qualified-string?)


;; Enable keyboard-support
;; ATM, only one collection can have keyboard-support
;; enabled at a time
(spec/def ::keyboard boolean?)


;; The list of items should always be distinct, so you would probably
;; pass it a `set`
(spec/def ::items (spec/coll-of ::item
                                :into #{}
                                :sorted? true
                                :distinct true))


(spec/def ::params
  (spec/keys :opt-un [::on-click
                      ::on-select
                      ::emphasize
                      ::deselectable
                      ::multiple
                      ::keyboard
                      ::addable
                      ::empty-message]))


(spec/def ::args
  (spec/cat :params ::params :items ::items))


;; Helper functions -------------------------------------------------------


(defn- next-item
  "Return the next item from collection"
  [coll item]
  (if (contains? coll item)
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
  (if (contains? coll item)
   (loop [prev (first coll)
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
    (let [pattern (re-pattern (str "(?iu)" substr))]
      (->> (-> (str/replace s pattern #(str ":" %1 ":"))
               (str/split #":"))
           (map-indexed #(if (util/=i %2 substr)
                           [:strong {:key (str "i-" %2 "-" %1 "-emp")} %2]
                           [:span {:key (str "i-" %2 "-" %1 "-no")} %2]))))))


;; TODO Scroll list upon navigating using keyboard
;; TODO Move keyboard-events out of the component
;; TODO Multiple selection using ctrl/shift
;; TODO Allow hiccup-content in the item?
(defn collection
  [& args]
  (let [intended* ^{:doc "Items that are intermediately marked and for selection"} (atom nil)
        selected* ^{:doc "A set of items that's been selected"} (atom #{})]
    (fn [& args]
      (let [{:keys [params items]}        (spec/conform ::args args)
            {:keys [emphasize
                    multiple
                    deselectable
                    keyboard
                    addable
                    empty-message
                    on-select]
             :or   {keyboard      true
                    addable       false
                    empty-message false}} params
            items                         (set (vals items))]

        ;; State management
        (letfn [(set-intended! [item]
                  (reset! intended* item))
                (add-item! [item]
                  (when-not (empty? item)
                    (if (contains? @selected* item)
                      (when deselectable (swap! selected* #(set/difference % #{item})))
                      (if multiple
                        (swap! selected* conj item)
                        (reset! selected* #{item})))
                    (when (fn? on-select) (on-select @selected*))))
                (on-key-down [event]
                  (let [key (util/code->key (.-which event))]
                    (case key
                      "up"    (set-intended! (prev-item items @intended*))
                      "down"  (set-intended! (next-item items @intended*))
                      "enter" (add-item! @intended*)
                      true)))]
          #?(:cljs (when keyboard (set! (.-onkeydown js/document) on-key-down)))

          [:ul.Collection
           (when (and (not (empty? emphasize))
                      (not (false? addable)))
             (let [item    {:id nil :value emphasize}
                   addable (case (first addable) :bool "+ %" :str (second addable))]
               [:li {:class          (str/join " " [(when (= item @intended*) "intended")
                                                    (when (contains? @selected* item) "selected")])
                     :on-mouse-enter #(set-intended! item)
                     :on-click       #(add-item! item)}
                (emphasize-match (str/replace-first addable "%" emphasize) emphasize)]))

           (when (and (empty? items)
                      (not (false? empty-message)))
             [:li (emphasize-match (str/replace-first empty-message "%" emphasize) emphasize)])

           (doall
            (for [{:keys [id value label]
                   :or   {label value}
                   :as   item} items]
              [:li {:key            (str "item-" (last id))
                    :class          (str/join " " [(when (= item @intended*) "intended")
                                                   (when (contains? @selected* item) "selected")])
                    :on-mouse-enter #(set-intended! item)
                    :on-click       #(add-item! item)}
               (emphasize-match label emphasize)]))])))))

