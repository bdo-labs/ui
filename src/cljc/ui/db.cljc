(ns ui.db
  (:require [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as spec]))


(spec/def ::id nat-int?)
(spec/def ::text (spec/and string? #(> (count %) 3)))
(spec/def ::item (spec/keys :req-un [::id ::text]))
(spec/def ::items (spec/coll-of ::item))


(spec/def ::loading? boolean?)


(spec/def ::active-panel keyword?)


(spec/def ::db (spec/keys :req-un [::loading?
                                   ::active-panel]))


(def default-db
  {:active-panel                          :doc-panel
   :loading?                              true
   :ui.docs.auto-complete/multiple        false
   :ui.docs.auto-complete/disabled        false
   :ui.docs.inputs/collection             (first (mapv first (spec/exercise ::items 500)))})
