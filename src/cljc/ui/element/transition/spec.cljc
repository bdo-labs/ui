(ns ui.element.transition.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [ui.specs :as common]
            [ui.util :as util]))



(spec/def ::model (spec/nonconforming (spec/or :keyword #{:fade-in :fade-out
                                                          :zoom-in :zoom-out
                                                          :flip-horizontal-in :flip-horizontal-out
                                                          :flip-vertical-in :flip-vertical-out}
                                               :deref util/deref?)))

(spec/def ::bezier-number (spec/and number?
                                    #(>= % 0)
                                    #(<= % 1)))

(spec/def ::duration number?)
(spec/def ::timing-function
  (spec/nonconforming
   (spec/or :keyword #{:linear :ease :ease-in :ease-out :ease-in-out :step-start :step-end :initial :inherit}
            ;; :steps   (spec/tuple #{:steps} number #{:start :end})
            ;; :cubic-bezier (spec/tuple #{:cubic-bezier} ::bezier-number ::bezier-number ::bezier-number ::bezier-number)
            )))
(spec/def ::delay number?)
(spec/def ::iteration-count (spec/nonconforming (spec/or :number number?
                                                         :infinite #{:infinite})))
(spec/def ::direction #{:normal :reverse :alternate :alternate-reverse :initial :inherit})
(spec/def ::fill-mode #{:none :forwards :backwards :both :initial :inherit})
(spec/def ::play-state #{:paused :running :initial :inherit})

(spec/def ::animation (spec/keys :opt-un [::duration
                                          ::timing-function
                                          ::delay
                                          ::iteration-count
                                          ::direction
                                          ::fill-mode
                                          ::play-state]))

(spec/def ::body any?)
(spec/def ::params
  (spec/keys :opt-un [::common/id
                      ::animation
                      ::visible?]
             :req-un [::model]))

(spec/def ::args (spec/cat :params ::params :body ::body))
