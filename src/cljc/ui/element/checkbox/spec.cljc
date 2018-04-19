(ns ui.element.checkbox.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [ui.specs :as common]))

(spec/def ::label string?)
(spec/def ::model
  (spec/nonconforming
   (spec/or :deref   (spec/and ::common/ratom #(let [v (deref %)]
                                                 (#{:checked :indeterminate :not-checked} v)))
            :boolean boolean?
            :nil     nil?
            :keywords #{:checked :indeterminate :not-checked})))

(spec/def ::on-change ::common/maybe-fn)

(spec/def ::params
  (spec/keys
   :opt-un [::common/id
            ::on-change]))

(spec/def ::--params
  (spec/merge
   ::params
   (spec/keys :opt-un [::model])))

(spec/def ::args (spec/cat :params ::--params :label (spec/? ::label)))

(spec/fdef checkbox
           :args ::args
           :ret vector?)
