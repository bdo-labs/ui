(ns ui.element.button.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [ui.element.icon.views :as icon]
            [ui.specs :as common]))

(spec/def ::fill boolean?)
(spec/def ::flat boolean?)
(spec/def ::raised boolean?)
(spec/def ::tab boolean?)
(spec/def ::pill boolean?)
(spec/def ::circular boolean?)
(spec/def ::disabled boolean?)
(spec/def ::content ::common/hiccup)

(spec/def ::class
  (spec/with-gen string?
    #(spec/gen #{"primary" "secondary" "tertiary" "positive" "negative" ""})))

(spec/def ::params
  (spec/keys :opt-un [::flat
                      ::fill
                      ::raised
                      ::pill
                      ::circular
                      ::tab
                      ::disabled
                      ::class]))

(spec/def ::args
  (spec/cat :params (spec/? ::params)
            :content (spec/* ::content)))

(spec/def ::ret
  (spec/cat :element #{:button.Button}
            :params (spec/keys :opt-un [::class])
            :content ::content))
