(ns ui.element.modal.spec
  (:require [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as spec]))

(spec/def ::stub
  (spec/with-gen fn?
    (gen/return (constantly nil))))

(spec/def ::show? boolean?)
(spec/def ::backdrop? boolean?)
(spec/def ::close-button? boolean?)
(spec/def ::cancel-on-backdrop? boolean?)
(spec/def ::on-cancel ::stub)
(spec/def ::hide ::stub)

(spec/def ::params
  (spec/keys :req-un [::on-cancel]
             :opt-un [::show?
                      ::close-button?
                      ::backdrop?
                      ::cancel-on-backdrop?]))

(spec/def ::content (spec/* (spec/or :str string? :vec vector?)))

(spec/def ::args
  (spec/cat :params ::params
            :content ::content))

(spec/def ::confirm-label string?)
(spec/def ::cancel-label string?)
(spec/def ::on-confirm ::stub)

(spec/def ::confirm-params
  (spec/keys :opt-un [::confirm-label
                      ::cancel-label]
             :req-un [::on-confirm
                      ::on-cancel]))

(spec/def ::confirm-args
  (spec/cat :params ::confirm-params
            :content ::content))
