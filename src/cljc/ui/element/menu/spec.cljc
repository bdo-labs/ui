(ns ui.element.menu.spec
  (:require [clojure.spec.alpha :as spec]))

;; FIXME Close drop-down upon clicking outside
;; it should also close when holding the mouse outside for a longer period of time

(spec/def ::open? boolean?)
(spec/def ::content-type (spec/or :nil nil? :seq seq? :str string? :vec vector?))
(spec/def ::variable-content (spec/* ::content-type))
(spec/def ::origins #{:top :bottom :left :right :center})
(spec/def ::origin
  (spec/coll-of ::origins :count 2))

(spec/def ::dropdown-params
  (spec/keys :req-un [::open?]
             :opt-un [::origin]))

(spec/def ::dropdown-args
  (spec/cat :params ::dropdown-params
            :content ::variable-content))

