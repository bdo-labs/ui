(ns ui.element.label.spec
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::params
  (spec/keys :req-un [::value]
             :opt-un [::id ::on-key-down]))

(spec/def ::args
  (spec/cat :params ::params))

