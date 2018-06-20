(ns ui.element.clamp.spec
  (:require [clojure.spec :as spec]
            [ui.specs :as common]))

(spec/def ::model ::common/model)

(spec/def ::params
  (spec/keys :opt-un [::model]))

(spec/def ::args
  (spec/cat :params ::params))
