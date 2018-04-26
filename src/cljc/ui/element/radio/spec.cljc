(ns ui.element.radio.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [ui.specs :as common]
            [ui.util :as util]))


(spec/def ::on-change   ::common/maybe-fn)
(spec/def ::render #{:horizontal :vertical})
(spec/def ::model (spec/nonconforming (spec/or :string string? :keyword keyword? :deref util/deref?)))

(spec/def ::label any?)
(spec/def ::value any?)
(spec/def ::id (spec/nonconforming (spec/or :string string? :keyword keyword?)))

(spec/def ::button (spec/keys :req-un [::id
                                       ::label]))
(spec/def ::buttons (spec/coll-of ::button))

(spec/def ::params
  (spec/keys :opt-un [::common/id
                      ::on-change
                      ::render]
             :req-un [::model
                      ::buttons]))
(spec/def ::args (spec/cat :params ::params))
