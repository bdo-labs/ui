(ns ui.element.period-picker.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]))

(spec/def ::stub
  (spec/with-gen fn?
    (gen/return (constantly nil))))

(spec/def ::id (spec/and string? #(not (= "" %))))
(spec/def ::from inst?)
(spec/def ::to inst?)
(spec/def ::min inst?)
(spec/def ::max inst?)
(spec/def ::start-of-week (spec/and integer? #(>= % 0) #(<= % 6)))
(spec/def ::formatter-args (spec/cat :format string? :time inst?))

(spec/def ::format
  (spec/with-gen string?
    #(spec/gen #{"E MMM d yyyy H:m" "E MMM d yyyy" "yyyyMMdd" "yyyyMMdd'T'HHmmss.SSSZ"
                 "yyyyMMdd'T'HHmmssZ yyyy-MM-dd'T'HH" "yyyy-MM-dd"})))
(spec/def ::time-picker boolean?)
(spec/def ::read-only boolean?)
(spec/def ::disabled boolean?)

;; (spec/def ::on-change ::stub)
;; (spec/def ::on-selected ::stub)


(spec/def ::params
  (spec/keys :req-un [::id]
             :opt-un [::from ::to ::min ::max ::start-of-week ::format
                      ::time-picker ::read-only ::disabled
                      ;; Listeners
                      ;; ::on-change ::on-selected
]))

(spec/def ::args
  (spec/cat :params ::params))

(spec/fdef formatter
           :args ::formatter-args
           :ret string?)

(spec/fdef display-formatter
           :args (spec/cat :formatter ::formatter-args :from ::from :to ::to)
           :ret string?
           :fn #(< (:from (:args %)) (:to (:args %))))

