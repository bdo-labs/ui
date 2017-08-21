(ns ui.db
  (:require [clojure.test.check.generators :as gen]
            [clojure.spec :as spec]))


(spec/def ::loading? boolean?)
(spec/def ::active-panel keyword?)
(spec/def ::db (spec/keys :req-un [::loading?
                                   ::active-panel]))


(def default-db
  {:active-panel                   :doc-panel
   :loading?                       true})
