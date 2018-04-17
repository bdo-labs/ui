(ns ui.element.chooser.spec
  (:require [clojure.spec.alpha :as spec]
            [ui.element.collection.spec :as collection]
            [ui.element.textfield.spec :as textfield]
            [clojure.test.check.generators :as gen]
            [ui.specs :as common]))

(spec/def ::close-on-select boolean?)
(spec/def ::labels boolean?)
(spec/def ::searchable boolean?)
(spec/def ::deletable boolean?)
(spec/def ::predicate? ::common/maybe-fn)

(spec/def ::params
  (spec/merge ::collection/params
              ::textfield/params
              (spec/keys :opt-un [::close-on-select
                                  ::deletable
                                  ::searchable
                                  ::labels
                                  ::predicate?])))

(spec/exercise ::params)

(spec/def ::args
  (spec/cat :params ::params))

