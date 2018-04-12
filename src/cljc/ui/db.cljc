(ns ui.db
  (:require [clojure.spec.alpha :as spec]
            [ui.docs.card :as card]))

(spec/def ::loading? boolean?)
(spec/def ::active-panel keyword?)

(spec/def ::db (spec/keys :req-un []))

(def default-db
  (merge {}
         card/db))
