(ns ui.element.containers.spec
  (:require [clojure.spec.alpha :as spec]
            [ui.specs :as common]))

;; Parameters
(spec/def ::background (spec/nonconforming (spec/or :str string? :key keyword?)))
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
(spec/def ::width (spec/or :flex int? :width string?))
(spec/def ::container-params
  (spec/keys :opt-un [::compact?
                      ::fill?
                      ::gap?
                      ::inline?
                      ::raised?
                      ::rounded?
                      ::wrap?
                      ::scrollable?
                      ::layout
                      ::background
                      ::align
                      ::space
                      ::width]))

;; Content
(spec/def ::hiccup (spec/nonconforming (spec/or :nil nil? :seq seq? :fn fn? :str string? :vec vector?)))
(spec/def ::content (spec/* ::hiccup))
;; (spec/def ::content ::common/hiccup)

;; Arguments
(spec/def ::container-args
  (spec/cat :params (spec/? ::container-params)
            :content ::content))

(spec/def ::open boolean?)
(spec/def ::ontop boolean?)
(spec/def ::backdrop boolean?)
(spec/def ::locked boolean?)
(spec/def ::on-click-outside fn?)
(spec/def ::to-the #{"left" "right"})
(spec/def ::sidebar-args (spec/keys :opt-un [::open ::ontop ::locked ::backdrop ::to-the ::on-click-outside]))
