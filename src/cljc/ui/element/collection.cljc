(ns ui.element.collection
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec :as spec]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]
            [ui.util :as util]))


;; Specification ----------------------------------------------------------


(spec/def ::maybe-fn
  (spec/with-gen fn?
    (gen/return (constantly nil))))


(spec/def ::qualified-string (spec/and string? not-empty))


;; When navigating using a keyboard, this is the item in focus
(spec/def ::intended ::id)


;; Substring to emphasize in the item-labels
(spec/def ::emphasize string?)


;; These are attributes of an item
;; Label will equal value if it's not explicitly set
(spec/def ::id (spec/or :str ::qualified-string :num nat-int?))
(spec/def ::value ::qualified-string)
(spec/def ::label ::qualified-string)
(spec/def ::item
  (spec/or
   :without-label (spec/cat :id ::id
                            :value ::value)
   :with-label (spec/cat :id ::id
                         :value ::value
                         :label ::label)
   :qualified (spec/keys :req-un [::id ::value]
                         :opt-un [::label])))


;; The list of items should always be distinct, so you would probably
;; pass it a `set`
(spec/def ::items (spec/coll-of ::item :distinct true))


(spec/def ::params
  (spec/keys :opt-un [::intended
                      ::emphasize]))


(spec/def ::args
  (spec/cat :params ::params :items ::items))


(defn collection
  [& args]
  (let [{:keys [params items]} (util/conform-or-fail ::args args)]
    [:ul.Collection
     (for [{:keys [id value label]
            :or {label value}} items]
       [:li {:key (str "item-" id)}
        label])]))


#_(defn collection
  [{:keys [on-click on-mouse-enter predicate]}]
  (fn [{:keys [items show class select]} element]
    (let [items (->> items
                     (sort #(compare (last %1) (last %2))))
          bold-match #(if (=i % show) [:strong %] [:span %])]
      [:div {:class (util/names->str class)}
       [:ul.Collection {:ref #(reset! element %)}
        (when (not-empty items)
          (map-indexed (fn [n {:keys [id text] :as item}]
                         (let [text (if-not (= "" show)
                                      (let [pattern #?(:clj (re-pattern (str "(?iu)" (str/lower-case show)))
                                                       :cljs (js/RegExp. (str/lower-case show) "ig"))]
                                        (str/replace text pattern #(str ":" %1 ":")))
                                      text)]
                           (into [:li {:key            (str "item-" n)
                                       :value          id
                                       :on-mouse-enter #(on-mouse-enter n)
                                       :class          (when (= select n) "Selected")
                                       :on-click       #(when (fn? on-click)
                                                          (on-click item))}]
                                 (->> (str/split text #":")
                                      (map bold-match)))))
                       items))]])))


#_(spec/fdef collection
        :args ::args
        :ret vector?)

