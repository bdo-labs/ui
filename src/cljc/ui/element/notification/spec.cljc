(ns ui.element.notification.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [ui.specs :as common]
            [ui.util :as util]))

(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))
(spec/def ::value string?)
(spec/def ::model util/deref?)
(spec/def ::class (spec/coll-of string?))
(spec/def ::params
  (spec/keys :opt-un [::common/id
                      ::value
                      ::class
                      ::model]))


(spec/def ::args (spec/cat :params ::params))


(spec/def ::notification ::params)
(spec/def ::notifications-params (spec/keys :opt-un [::common/id
                                                     ::class
                                                     ::notification]
                                            :req-un [::model]))
(spec/def ::notifications-args (spec/cat :params ::notifications-params))
