(ns ui.element.collection.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]))

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
(spec/def ::predicate? ::maybe-fn)

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
                      ::predicate?
                      ::add-message
                      ::empty-message
                      ::hide-selected]))

(spec/def ::args
  (spec/cat :params ::params :items ::items))

