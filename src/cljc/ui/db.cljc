(ns ui.db
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::loading? boolean?)
(spec/def ::active-panel keyword?)

(spec/def ::db (spec/keys :req-un []))

(def default-db
  {})
