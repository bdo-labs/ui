(ns ui.element.chooser.spec
  (:require [clojure.spec.alpha :as spec]
            [ui.element.collection.spec :as collection]
            [ui.element.textfield.spec :as textfield]
            [clojure.test.check.generators :as gen]))

(spec/def ::maybe-fn
  (spec/with-gen fn?
    (gen/return (constantly nil))))

(spec/def ::close-on-select boolean?)
(spec/def ::labels boolean?)
(spec/def ::searchable boolean?)
(spec/def ::predicate? ::maybe-fn)
(spec/def ::deletable boolean?)

(spec/def ::params
  (spec/merge ::collection/params
              ::textfield/--params
              (spec/keys :opt-un [::close-on-select
                                  ::deletable
                                  ::searchable
                                  ::labels
                                  ::predicate?])))

(spec/def ::args
  (spec/cat :params ::params))

