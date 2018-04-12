(ns ui.element.notification.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [ui.specs :as common]))

(spec/def ::params
  (spec/keys :opt-un []))

(spec/def ::content)

(spec/def ::args
  (spec/cat :params ::params :content ::content))
