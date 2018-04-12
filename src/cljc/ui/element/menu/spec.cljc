(ns ui.element.menu.spec
  (:require [clojure.spec.alpha :as spec]
            [ui.specs :as common]))

(spec/def ::open? boolean?)
(spec/def ::origins #{:top :bottom :left :right :center})
(spec/def ::origin
  (spec/coll-of ::origins :count 2))
(spec/def ::on-click-outside ::common/maybe-fn)

(spec/def ::params
  (spec/keys :req-un [::open?]
             :opt-un [::origin
                      ::on-click-outside]))

(spec/def ::content (spec/* ::common/hiccup))

(spec/def ::args
  (spec/cat :params ::params
            :content ::content))

