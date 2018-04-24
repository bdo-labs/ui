(ns ui.element.collection.views
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            #?(:cljs [reagent.core :as reagent])
            [clojure.string :as str]
            [clojure.set :as set]
            [ui.element.button.views :refer [button]]
            [ui.element.icon.views :refer [icon]]
            [ui.element.collection.spec :as spec]
            [ui.util :as util]
            [re-frame.core :as re-frame]))

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

;; TODO Scroll list upon navigating using keyboard
;; TODO Multiple selection using ctrl/shift
;; TODO Allow custom markup in each item?

(defn collection [& args]
  (let [{:keys [params]}        (util/conform! ::spec/args args)
        {:keys [id
                class
                items
                emphasize
                model
                keyboard
                max-selected
                expanded
                collapsable
                selectable
                deselectable
                multiple
                on-toggle-expand
                on-click
                on-select
                on-mouse-enter]
         :or   {id          (util/gen-id)
                expanded    false
                collapsable false
                keyboard    true
                selectable  false}} params
        id                      (util/slug id)
        expanded*               (atom expanded)
        intended*               (atom nil)
        ui-params               (select-keys params (util/keys-from-spec ::spec/params))
        items                   (apply sorted-set-by (fn [a b] (< (:value a) (:value b))) items)]
    (letfn [(set-intended! [item]
              (reset! intended* item))
            (remove-item! [item]
                          (swap! model disj item))
            (add-item! [item]
              (when (seq item)
                         (if (contains? @model item)
                           (when deselectable
                             (if (= 1 (count @model))
                               (remove-item! item)
                               (reset! model (set/difference @model #{item}))))
                           (when (or (nil? max-selected)
                                     (> max-selected (count @model)))
                             (if (or (not multiple)
                                     (empty? @model))
                               (reset! model #{item})
                               (swap! model conj item)))))
                       (when (fn? on-select)
                         (on-select @model)))
            (--on-key-down [event]
                           (when (and selectable
                                      (seq @intended*))
                             (let [key (util/code->key (.-which event))]
                               (case key
                                 "up"    (set-intended! (prev-item items @intended*))
                                 "down"  (set-intended! (next-item items @intended*))
                                 "enter" (add-item! @intended*)
                                 "esc"   (reset! intended* nil)
                                 true))))
            (--on-mouse-enter [item event]
                              (when selectable (set-intended! item)
                                    (when (ifn? on-mouse-enter) (on-mouse-enter item event))))
            (--on-click [item event]
                        (when selectable (add-item! item)
                              (when (ifn? on-click) (on-click item event))))
            (toggle-expanded []
                             (do (swap! expanded* not)
                                 (when (ifn? on-toggle-expand) (on-toggle-expand @expanded*))))
            (render-fn [& args]
                       (let [{:keys [params items]} (util/conform! ::spec/args args)
                             intended               @intended*
                             expanded?              @expanded*
                             items                  (apply sorted-set-by (fn [a b] (< (:value a) (:value b))) items)
                             items                  (if (or (not collapsable) expanded?) items (take 1 items))]
                         [:ul.Collection {:key   (util/slug "collection" id)
                                          :class (str (util/params->classes ui-params) " " class)}
                          (when (seq items)
                            ;; force evaluation. React doesn't like a LazySeq here
                            (doall
                             (for [{:keys [id value label class] :as item} items]
                               (let [label (or label value)]
                                 (into
                                  [:li {:key            (util/slug "collection" "item" id)
                                        :class          (str/join " " [(when (= (:id item) (:id intended)) "intended")
                                                                       (when (and (util/deref? model)
                                                                                  (contains? @model item)) "selected")
                                                                       (str class)])
                                        :on-mouse-enter (partial --on-mouse-enter item)
                                        :on-click       (partial --on-click item)}]
                                  (if (and collapsable
                                           (= (:id (first items)) id))
                                    [[:div.item-area
                                      (if (string? label)
                                        (emphasize-match label emphasize)
                                        label)]
                                     [:div.collapse-area
                                      [button {:on-click toggle-expanded}
                                       [icon {:size 2} (str "chevron-" (if expanded? "up" "down"))]]]]
                                    [(if (string? label)
                                       (emphasize-match label emphasize)
                                       label)]))))))]))]
      #?(:clj render-fn
         :cljs
         (reagent/create-class
          {:display-name           "collection"
           :component-did-mount    #(when keyboard (.addEventListener js/document "keydown" --on-key-down))
           :component-will-unmount #(when keyboard (.removeEventListener js/document "keydown" --on-key-down))
           :reagent-render         render-fn})))))
