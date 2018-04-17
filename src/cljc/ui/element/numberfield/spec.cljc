(ns ui.element.numberfield.spec
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [clojure.test.check.generators :as gen]
            [ui.util :as util]))


(spec/def ::maybe-fn
  (spec/with-gen fn?
    (gen/return (constantly nil))))


;; Events
(spec/def ::on-change ::maybe-fn)
(spec/def ::on-focus ::maybe-fn)
(spec/def ::on-blur ::maybe-fn)
(spec/def ::on-key-up ::maybe-fn)
(spec/def ::on-key-down ::maybe-fn)


;; Parameters
(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))
(spec/def ::placeholder #(or (string? %) (nil? %)))
(spec/def ::max (spec/or :number number? :nil nil?))
(spec/def ::min (spec/or :number number? :nil nil?))
(spec/def ::step (spec/or :number number? :nil nil?))
(spec/def ::label (spec/or :string string? :number number?))
(spec/def ::disabled boolean?)
(spec/def ::auto-focus boolean?)
(spec/def ::read-only boolean?)
(spec/def ::focus boolean?)
(spec/def ::model util/deref?)
(spec/def ::--params
  (spec/keys :opt-un [::id
                      ::placeholder
                      ::auto-focus
                      ::label
                      ::disabled
                      ::read-only
                      ::focus
                      ::max
                      ::min
                      ::step
                      ::on-change
                      ::on-focus
                      ::on-blur
                      ::on-key-up
                      ::on-key-down]))

(spec/def ::params
  (spec/merge ::--params
              (spec/keys :opt-un [::model])))


(spec/def ::args (spec/cat :params ::params))
