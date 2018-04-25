(ns ui.element.collection.spec
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [ui.specs :as common]))

;; Parameters
(spec/def ::model
  (spec/with-gen ::common/ratom
    #(gen/fmap atom (spec/gen ::items)))) ;; Set of selected items
(spec/def ::multiple boolean?) ;; Wether you can select multiple items
(spec/def ::emphasize string?) ;; Substring to emphasize in item-labels
(spec/def ::sorted boolean?) ;; Wether the set of items should be sorted
(spec/def ::predicate? ::common/maybe-fn) ;; Predicate to use when emphasizing a substring of each item
(spec/def ::collapsable boolean?) ;; Expand/collapse for lists with multiple items (defaults to false)
(spec/def ::expanded boolean?) ;; Wether the collection should be expanded by default
(spec/def ::selectable boolean?) ;; Makes it possible to select items
(spec/def ::deselectable boolean?) ;; Makes it possible to de-select items
(spec/def ::hide-selected boolean?) ;; Hides already selected elements from collection
(spec/def ::add-message ::common/qualified-string?) ;; Add free-text item to collection
(spec/def ::empty-message ::common/qualified-string?) ;; Text to display when there's no matches
(spec/def ::max-selected nat-int?) ;; Maximum number of items selected. Only used in correspondance with multiple
(spec/def ::keyboard boolean?) ;; Enable/disable keyboard-support

;; Events
(spec/def ::on-toggle-expand ::common/maybe-fn)
(spec/def ::on-click ::common/maybe-fn)
(spec/def ::on-select ::common/maybe-fn)
(spec/def ::on-mouse-enter ::common/maybe-fn)

(spec/def ::params
  (spec/keys :opt-un [;; Params
                      ::common/id
                      ::model
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
                      ;; Events
                      ::on-toggle-expand
                      ::on-click
                      ::on-select
                      ::on-mouse-enter]))

;; Item
(spec/def ::id (spec/nonconforming (spec/or :str :ui.specs/qualified-string? :num nat-int?)))
(spec/def ::value ::common/qualified-string?)
(spec/def ::label ::common/hiccup)
(spec/def ::item (spec/keys :req-un [::id ::value] :opt-un [::label]))
(spec/def ::items (spec/coll-of ::item))

(spec/def ::args
  (spec/cat :params ::params :items ::items))

