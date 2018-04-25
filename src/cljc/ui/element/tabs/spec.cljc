(ns ui.element.tabs.spec
  (:require [clojure.spec.alpha :as spec]
            [ui.element.tabs.tab-spec :as tab-spec]
            [ui.specs :as common]
            [ui.util :as util]))


(spec/def ::on-change ::common/maybe-fn)

(spec/def ::render #{:horizontal :horizontal-bars :vertical :vertical-bars})
(spec/def ::model (spec/nonconforming (spec/or :string string? :keyword keyword? :deref util/deref?)))
(spec/def ::sheet ifn?)
(spec/def ::sheets (spec/coll-of ::sheet))
(spec/def ::class (spec/map-of ::tab-spec/id ::sheet))
(spec/def ::params
  (spec/keys :opt-un [::common/id
                      ::class
                      ::on-change
                      ::render
                      ::sheets]
             :req-un [::model
                      ::tab-spec/tabs]))

(spec/def ::args (spec/cat :params ::params))
