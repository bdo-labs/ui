(ns ui.element.collection.spec
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [ui.specs :as common]))

;; Events
(spec/def ::on-toggle-expand ::common/maybe-fn)
(spec/def ::on-click ::common/maybe-fn)
(spec/def ::on-select ::common/maybe-fn)
(spec/def ::on-mouse-enter ::common/maybe-fn)

;; Parameters
(spec/def ::model
  (spec/with-gen (spec/and ::common/ratom #(let [value (deref %)]
                                             (or (empty? value) (set? value))))
    #(gen/fmap atom (spec/gen ::items))))
(spec/def ::multiple boolean?) ;; Wether you can select multiple items
(spec/def ::emphasize string?) ;; Substring to emphasize in the item-labels
(spec/def ::predicate? ::common/maybe-fn) ;; Predicate to use when emphasizing a substring of each item
(spec/def ::collapsable boolean?) ;; Expand/collapse for lists with multiple items (defaults to false)
(spec/def ::expanded boolean?) ;; Wether the collection should be expanded by default
(spec/def ::selectable boolean?) ;; Makes it possible to selection items
(spec/def ::deselectable boolean?) ;; Makes it possible to de-select items
(spec/def ::hide-selected boolean?) ;; Hides already selected elements from collection
(spec/def ::add-message ::common/qualified-string?) ;; Add free-text item to collection
(spec/def ::empty-message ::common/qualified-string?) ;; Text to display when there's no matches
(spec/def ::max-selected nat-int?) ;; Maximum number of items selected. Only used in correspondance with multiple
(spec/def ::keyboard boolean?) ;; Enable keyboard-support
(spec/def ::params
  (spec/keys :opt-un [::common/id
                      ::max-selected
                      ::emphasize
                      ::collapsable
                      ::selectable
                      ::deselectable
                      ::multiple
                      ::keyboard
                      ::predicate?
                      ::add-message
                      ::empty-message
                      ::hide-selected
                      ::on-toggle-expand
                      ::on-click
                      ::on-select
                      ::on-mouse-enter]))
(spec/def ::--params
  (spec/merge ::params
              (spec/keys :opt-un [::model])))

(spec/def ::id (spec/nonconforming (spec/or :str :ui.specs/qualified-string? :num nat-int?)))
(spec/def ::value :ui.specs/qualified-string?)
(spec/def ::label ::common/hiccup)
(spec/def ::item (spec/keys :req-un [::id ::value] :opt-un [::label]))
(spec/def ::items
  (spec/with-gen
    (spec/coll-of ::item :kind? set)
    #(gen/fmap set (spec/gen (spec/coll-of ::item)))))

(spec/def ::args
  (spec/cat :params ::--params
            :items ::items))
