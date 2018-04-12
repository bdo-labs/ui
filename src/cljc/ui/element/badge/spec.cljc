(ns ui.element.badge.spec
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::show-content? boolean?)

(spec/def ::params
  (spec/keys :opt-un [::show-content?]))

(spec/def ::content
  (spec/or :number nat-int?
           :string string?))

(spec/def ::args
  (spec/cat :params (spec/? ::params)
            :content ::content))
