(ns ui.element.breadcrumbs.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [ui.specs :as common]
            [ui.util :as util]))


(spec/def ::crumb (spec/tuple string? string?))
(spec/def ::model (spec/nonconforming (spec/or :string string?
                                               :crumbs (spec/coll-of ::crumb)
                                               :deref  util/deref?)))

(spec/def ::params
  (spec/keys :opt-un [::common/id]
             :req-un [::model]))

(spec/def ::args (spec/cat :params ::params))
