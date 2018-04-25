(ns ui.element.tabs.tab-spec
  (:require [clojure.spec.alpha :as spec]
            [ui.specs :as common]
            [ui.util :as util]))

(spec/def ::id (spec/nonconforming (spec/or :string string? :keyword keyword?)))
(spec/def ::label string?)
(spec/def ::tab (spec/keys :req-un [::id]
                           :opt-un [::label]))

(spec/def ::tabs (spec/nonconforming (spec/or :tabs (spec/coll-of ::tab)
                                              :deref util/deref?)))
