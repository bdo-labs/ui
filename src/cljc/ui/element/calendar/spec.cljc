(ns ui.element.calendar.spec
  (:require [clojure.test.check.generators :as gen]
            [#?(:clj clj-time.core :cljs cljs-time.core) :as time]
            [clojure.spec.alpha :as spec]))

(spec/def ::stub
  (spec/with-gen fn?
    (gen/return (constantly nil))))

(spec/def ::start-of-week (spec/and integer? #(>= % 0) #(<= % 6)))
(spec/def ::show-weekend? boolean?)
(spec/def ::nav? boolean?)
(spec/def ::selectable? boolean?)
(spec/def ::multiple? boolean?)
(spec/def ::short-form? boolean?)
(spec/def ::jump pos-int?)
(spec/def ::on-navigation ::stub)
(spec/def ::every (spec/and pos-int? #(int? (/ 12 %)) #(<= % 6)))
(spec/def ::selected #?(:clj inst? :cljs time/date?))
(spec/def ::on-click ::stub)

(spec/def ::years-params
  (spec/keys :opt-un [::on-click]))
(spec/def ::years-args (spec/cat :params ::years-params))

(spec/def ::months-params
  (spec/keys :req-un [::on-click]
             :opt-un [::selected ::every]))
(spec/def ::months-args
  (spec/cat :params ::months-params))

(spec/def ::days-params
  (spec/keys :req-un [::on-click]
             :opt-un [::on-navigation
                      ::jump
                      ::short-form?
                      ::selectable?
                      ::nav?
                      ::show-weekend?
                      ::start-of-week]))
(spec/def ::days-args
  (spec/cat :params ::days-params))
