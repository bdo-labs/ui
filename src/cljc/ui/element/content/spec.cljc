(ns ui.element.content.spec
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::class string?)

(spec/def ::article-params
  (spec/keys :opt-un [::class]))

(spec/def ::content
  (spec/nonconforming
   (spec/or :str string?
            :vec vector?
            :nil nil?)))

(spec/def ::article
  (spec/cat :params (spec/? ::article-params)
            :content (spec/* ::content)))

