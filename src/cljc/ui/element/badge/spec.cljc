(ns ui.element.badge.spec
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::show-count boolean?)

(spec/def ::params
  (spec/keys :opt-un [::show-count]))

(spec/def ::content
  (spec/nonconforming
   (spec/or :num nat-int?
            :nil nil?)))

(spec/def ::args
  (spec/cat :params (spec/? ::params)
            :content ::content))
