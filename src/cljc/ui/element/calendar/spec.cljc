(ns ui.element.calendar.spec
  (:require [clojure.test.check.generators :as gen]
            #?(:cljs [cljs-time.core :refer [date?]])
            #?(:clj [clj-time.types :refer [date-time?]])
            [clojure.spec.alpha :as spec]
            [ui.specs :as common]))

#?(:cljs (defn date-time? [d] (date? d)))

(spec/def ::start-of-week (spec/and integer? #(>= % 0) #(<= % 6)))
(spec/def ::show-weekend? boolean?)
(spec/def ::nav? boolean?)
(spec/def ::selectable? boolean?)
(spec/def ::multiple? boolean?)
(spec/def ::short-form? boolean?)
(spec/def ::jump pos-int?)
(spec/def ::on-navigation ::common/maybe-fn)
(spec/def ::every (spec/and pos-int? #(int? (/ 12 %)) #(<= % 6)))
(spec/def ::period (spec/coll-of date-time? :count 2))
(spec/def ::model
  (spec/nonconforming
   (spec/or :ratom ::common/ratom
            :period ::period)))
(spec/def ::on-click ::common/maybe-fn)

(spec/def ::years-params
  (spec/keys :opt-un [::on-click]))
(spec/def ::years-args (spec/cat :params ::years-params))

(spec/def ::months-params
  (spec/keys :opt-un [::on-click ::model ::every]))
(spec/def ::months-args
  (spec/cat :params ::months-params))

(spec/def ::days-params
  (spec/keys :opt-un [::on-click
                      ::on-navigation
                      ::jump
                      ::short-form?
                      ::selectable?
                      ::nav?
                      ::show-weekend?
                      ::start-of-week
                      ::model]))
(spec/def ::days-args
  (spec/cat :params ::days-params))
