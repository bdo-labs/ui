(ns ui.element.textfield.spec
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [ui.util :refer [deref?]]
            [ui.specs :as common]))

;; Events
(spec/def ::on-change ::common/maybe-fn)
(spec/def ::on-focus ::common/maybe-fn)
(spec/def ::on-blur ::common/maybe-fn)
(spec/def ::on-key-up ::common/maybe-fn)
(spec/def ::on-key-down ::common/maybe-fn)

;; Parameters
(spec/def ::placeholder string?)
(spec/def ::label string?)
(spec/def ::model
  (spec/nonconforming
   (spec/with-gen (spec/or :deref (spec/and ::common/ratom #(string? (deref %)))
                           :string string?
                           :nil nil?)
     #(gen/fmap atom gen/string-alphanumeric))))
(spec/def ::disabled boolean?)
(spec/def ::auto-focus boolean?)
(spec/def ::read-only boolean?)
(spec/def ::params
  (spec/keys :opt-un [::common/id
                      ::placeholder
                      ::label
                      ::disabled
                      ::read-only
                      ::auto-focus
                      ::on-change
                      ::on-focus
                      ::on-blur
                      ::on-key-up
                      ::on-key-down]))

(spec/def ::--params
    (spec/merge ::params
                (spec/keys :opt-un [::model])))

(spec/def ::args (spec/cat :params ::--params))
