(ns ui.element.button.spec
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::fill boolean?)
(spec/def ::flat boolean?)
(spec/def ::raised boolean?)
(spec/def ::tab boolean?)
(spec/def ::pill boolean?)
(spec/def ::circular boolean?)
(spec/def ::disabled boolean?)
(spec/def ::content (spec/* (spec/or :str string? :vec vector?)))
(spec/def ::class
  (spec/with-gen string?
    #(spec/gen #{"primary" "secondary" "tertiary" "positive" "negative" ""})))

(spec/def ::params
  (spec/merge (spec/keys :opt-un [::flat ::fill ::raised
                                  ::pill ::circular ::tab
                                  ::disabled ::class])
              ;; :ui.element.ripple/params)
))

(spec/def ::args
  (spec/cat :params ::params
            :content ::content))

(spec/def ::ret
  (spec/cat :element #{:button.Button}
            :params (spec/keys :opt-un [::class])
            :content ::content))
