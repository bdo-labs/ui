(ns ui.element.textfield.spec
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [ui.util :refer [ratom?]]
            [ui.specs :as common]))

;; Events
(spec/def ::on-change ::common/maybe-fn)
(spec/def ::on-cancel ::common/maybe-fn)
(spec/def ::on-focus ::common/maybe-fn)
(spec/def ::on-blur ::common/maybe-fn)
(spec/def ::on-key-up ::common/maybe-fn)
(spec/def ::on-key-down ::common/maybe-fn)

;; Parameters
(spec/def ::placeholder string?)
(spec/def ::label string?)
(spec/def ::value
  (spec/nonconforming
   (spec/or :string string? :nil nil?)))
(spec/def ::model
  (spec/nonconforming
   (spec/with-gen (spec/and ::common/ratom #(string? (deref %)))
     #(gen/fmap atom gen/string-alphanumeric))))
(spec/def ::required boolean?)
(spec/def ::valid? ::common/maybe-fn)
(spec/def ::disabled boolean?)
(spec/def ::auto-focus boolean?)
(spec/def ::read-only boolean?)
(spec/def ::type (spec/and keyword? #{:search :text :password :email}))
(spec/def ::ref ::common/maybe-fn)
(spec/def ::params
  (spec/keys :opt-un [::common/id
                      ::ref
                      ::type
                      ::placeholder
                      ::label
                      ::model
                      ::value
                      ::disabled
                      ::required
                      ::valid?
                      ::read-only
                      ::auto-focus
                      ::on-change
                      ::on-focus
                      ::on-blur
                      ::on-key-up
                      ::on-key-down]))

#_(spec/def ::--params
    (spec/merge ::params
                (spec/keys :req-un [::model])))

(spec/def ::args (spec/cat :params ::params))
