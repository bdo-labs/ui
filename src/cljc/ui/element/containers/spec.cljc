(ns ui.element.containers.spec
  (:require [clojure.spec.alpha :as spec]))

;; Parameter specifications
(spec/def ::background (spec/or :str string?
                                :key keyword?))
(spec/def ::compact? boolean?)
(spec/def ::fill? boolean?)
(spec/def ::gap? boolean?)
(spec/def ::inline? boolean?)
(spec/def ::raised? boolean?)
(spec/def ::rounded? boolean?)
(spec/def ::wrap? boolean?)
(spec/def ::scrollable? boolean?)
(spec/def ::align (spec/coll-of ::alignment :min-count 1 :max-count 2))
(spec/def ::alignment #{:start :end :center})
(spec/def ::layout #{:horizontally :vertically})
(spec/def ::space #{:between :around :none})
(spec/def ::content-type (spec/or :nil nil? :seq seq? :fn fn? :str string? :vec vector?))
(spec/def ::variable-content (spec/* ::content-type))
(spec/def ::width
  (spec/or :flex int?
           :width string?))

;; Consolidated parameters
(spec/def ::container-params
  (spec/keys :opt-un [::compact? ::fill? ::gap? ::inline? ::raised? ::rounded? ::wrap? ::scrollable?
                      ::layout ::background ::align ::space ::width]))

;; Full arguments specifications
(spec/def ::container-args
  (spec/cat :params (spec/? ::container-params)
            :content ::variable-content))

(spec/def ::open boolean?)
(spec/def ::ontop boolean?)
(spec/def ::backdrop boolean?)
(spec/def ::locked boolean?)
(spec/def ::on-click-outside fn?)
(spec/def ::to-the #{"left" "right"})
(spec/def ::sidebar-args (spec/keys :opt-un [::open ::ontop ::locked ::backdrop ::to-the ::on-click-outside]))
