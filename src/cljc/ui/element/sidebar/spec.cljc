(ns ui.element.sidebar.spec
  (:require [clojure.spec.alpha :as spec]
            [ui.specs :as common]))

(spec/def ::open? boolean?)
(spec/def ::locked? boolean?)
(spec/def ::backdrop? boolean?)
(spec/def ::ontop? boolean?)
(spec/def ::to-the #{:left :right})
(spec/def ::on-click-outside ::common/maybe-fn)

(spec/def ::params
  (spec/keys :opt-un [::open?
                      ::locked?
                      ::backdrop?
                      ::ontop?
                      ::to-the
                      ::on-click-outside]))

(spec/def ::content vector?)

(spec/def ::args
  (spec/cat :params ::params
            :sidebar-content ::content
            :main-content ::content))
