(ns ui.element.paginator.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [ui.specs :as common]
            [ui.util :as util]))


(spec/def ::model (spec/nonconforming (spec/or :number number? :deref util/deref?)))
(spec/def ::count-per-page number?)
(spec/def ::length (spec/nonconforming (spec/or :sequential sequential? :number number?)))
(spec/def ::edge number?)

(spec/def ::params
  (spec/keys :opt-un [::common/id
                      ::count-per-page
                      ::edge]
             :req-un [::model
                      ::length]))

(spec/def ::args (spec/cat :params ::params))
