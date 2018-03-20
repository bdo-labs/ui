(ns ui.element.checkbox.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]))

(spec/def ::maybe-fn
  (spec/with-gen fn?
    (gen/return (constantly nil))))

(spec/def ::id (spec/or :numeric int? :textual (spec/and string? not-empty)))
(spec/def ::checked? boolean?)
(spec/def ::label string?)
(spec/def ::value string?)

(spec/def ::on-change ::maybe-fn)

(spec/def ::checkbox-params
  (spec/keys
   :opt-un [::id ::checked? ::value ::on-change]))

(spec/def ::checkbox-args
  (spec/cat :params ::checkbox-params :label ::label))

(spec/fdef checkbox
           :args ::checkbox-args
           :ret vector?)

