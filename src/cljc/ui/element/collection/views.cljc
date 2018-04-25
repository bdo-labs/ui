(ns ui.element.collection.views
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            #?(:cljs [reagent.core :as reagent])
            [clojure.string :as str]
            [clojure.data :refer [diff]]
            [ui.element.button.views :refer [button]]
            [ui.element.icon.views :refer [icon]]
            [ui.element.collection.spec :as spec]
            [ui.util :as util]))

;; Helper functions -------------------------------------------------------

(defn- next-item
  "Return the next item from collection"
  [coll item]
  (if (and (some? item)
           (nat-int? (.indexOf coll item)))
    (if (< (inc (.indexOf coll item)) (count coll))
      (nth coll (inc (.indexOf coll item)))
      (last coll))
    (first coll)))

(defn- prev-item
  "Return the previous item from collection"
  [coll item]
  (if (and (some? item)
           (nat-int? (.indexOf coll item)))
    (if (> (dec (.indexOf coll item)) 0)
      (nth coll (dec (.indexOf coll item)))
      (first coll))
    (last coll)))

(defn- multiple? [items]
  (and (seq items)
       (> (count items) 1)))

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

(defn collection [& args]
  (let [{:keys [params items]} (util/conform! ::spec/args args)
        {:keys [id
                model
                selectable
                deselectable
                max-selected
                expanded
                multiple
                on-toggle-expand
                on-click
                on-select
                on-mouse-enter]
         :or   {id         (util/gen-id)
                selectable false
                expanded   false}}   params
        id                     (util/slug id)
        on-key-down-listener*  (atom nil)
        multiple*              (atom multiple)
        selectable*            (atom selectable)
        deselectable*          (atom deselectable)
        max-selected*          (atom max-selected)
        items*                 (atom items)
        expanded*              (atom expanded)
        intended*              (atom nil)
        operation*             (atom nil)
        ui-params              (select-keys params (util/keys-from-spec ::spec/params))]

    (letfn [(set-intended! [item]
              (reset! intended* item))

            (remove-item! [item]
                          (reset! model (remove (partial = item) @model)))

            (add-item! [item]
                       (when (seq item)
                         (if (contains? (set @model) item)
                           (when @deselectable*
                             (remove-item! item))
                           (when (or (nil? @max-selected*)
                                     (> max-selected (count @model)))
                             (if (or (not @multiple*)
                                     (empty? @model))
                               (reset! model [item])
                               (swap! model conj item)))))
                       (when (ifn? on-select)
                         (on-select @model)))

            (re-align [element]
                      (when (and (seq @items*)
                                 (seq @intended*))
                        (let [position (first (remove nil? (map-indexed (fn [n v] (when (= v @intended*) n)) @items*)))
                              height   (.-offsetHeight (or (.querySelector element ".intended")
                                                           (.-firstChild element)))]
                          (if (> position 3)
                            (aset element "scrollTop" (- (* position height) (* height 2)))
                            (aset element "scrollTop" 0)))))

            (--on-mouse-enter [item event]
                              (when @selectable* (set-intended! item)
                                    (when (ifn? on-mouse-enter) (on-mouse-enter item event))))

            (--on-click [item event]
                        (when (and @selectable*
                                   (seq @intended*))
                          (add-item! item)
                          (when (ifn? on-click) (on-click item event))))

            (--on-toggle-expanded []
                                  (do (swap! expanded* not)
                                      (when (ifn? on-toggle-expand) (on-toggle-expand @expanded*))))

            (--on-key-down [element event]
                           (when @selectable*
                             (let [key (util/code->key (.-which event))]
                               (case key
                                 "up"    (do (set-intended! (prev-item @items* @intended*))
                                             (re-align element))
                                 "down"  (do (set-intended! (next-item @items* @intended*))
                                             (re-align element))
                                 "enter" (add-item! @intended*)
                                 "esc"   (reset! intended* nil)
                                 true))))

            (render-fn [& args]
                       (let [{:keys [params items]}  (util/conform! ::spec/args args)
                             {:keys [class
                                     selectable
                                     deselectable
                                     max-selected
                                     emphasize
                                     collapsable
                                     multiple
                                     predicate?
                                     add-message
                                     empty-message]
                              :or   {collapsable false
                                     predicate? str/includes?
                                     add-message false
                                     empty-message false}} params
                             intended                @intended*
                             expanded?               @expanded*
                             collapsable             (if (multiple? items) collapsable false)
                             items                   (if (or (not collapsable) expanded?) items (take 1 items))]
                         (do
                           (reset! items* items)
                           (reset! selectable* selectable)
                           (reset! multiple* multiple)
                           (reset! max-selected* max-selected)
                           [:ul.Collection {:key   (util/slug "collection" id)
                                            :class (str (util/params->classes ui-params) " " class)}

                            ;; Add new item
                            (when (and (seq emphasize)
                                       (not (false? add-message))
                                       (predicate? emphasize emphasize)
                                       (not= emphasize (:value (first items))))
                              (let [item {:id nil :value emphasize}]
                                [:li {:key            (util/slug id "adder")
                                      :class          (str/join " " [(when (= item @intended*) "intended")
                                                                     (when (contains? @model item) "selected")])
                                      :on-mouse-enter #(set-intended! item)
                                      :on-click       #(add-item! item)}
                                 (str/replace-first add-message "%" (str "\"" emphasize "\""))]))
                            ;; no results
                            (when (and (empty? items)
                                       (predicate? emphasize emphasize)
                                       (not (false? empty-message)))
                              [:li {:key (util/slug id "empty")}
                               (str/replace-first empty-message "%" (str "\"" emphasize "\""))])

                            (when (seq items)
                              (let [distinct-model (if (nil? model) #{} (set @model))]
                                (doall
                                 (for [{:keys [id value label class] :as item} items]
                                   (let [label (or label value)]
                                     (into
                                      [:li {:key            (util/slug "collection" "item" id)
                                            :class          (str/join " " [(when (= (:id item) (:id intended)) "intended")
                                                                           (when (and (util/ratom? model)
                                                                                      (contains? distinct-model item)) "selected")
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
                                          [button {:on-click --on-toggle-expanded}
                                           [icon {:size 2} (str "chevron-" (if expanded? "up" "down"))]]]]
                                        [(if (string? label)
                                           (emphasize-match label emphasize)
                                           label)])))))))])))]

      #?(:clj render-fn
         :cljs (reagent/create-class
                {:display-name           "collection"
                 :component-did-mount    #(when-not @on-key-down-listener*
                                            (reset! on-key-down-listener* (partial --on-key-down (reagent/dom-node %)))
                                            (.addEventListener js/document "keydown" @on-key-down-listener* true))
                 :component-will-unmount #(when @on-key-down-listener*
                                            (.removeEventListener js/document "keydown" @on-key-down-listener* true)
                                            (reset! on-key-down-listener* nil))
                 :reagent-render         render-fn})))))
