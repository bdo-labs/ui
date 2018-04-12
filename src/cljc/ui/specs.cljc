(ns ui.specs
  "Common specifications"
  #?(:clj (:import [clojure.lang IDeref]))
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [ui.util :refer [ratom?]]))

(spec/def ::id
  (spec/and string? #(re-find #"(?i)(\w+)" %)))

(spec/def ::ratom
  (spec/with-gen (spec/and ratom? #(satisfies? IDeref %))
    #(gen/return (atom %))))

(spec/def ::qualified-string?
  (spec/and string? not-empty))

(spec/def ::maybe-fn
  (spec/with-gen ifn?
    #(gen/return (constantly nil))))

(spec/def ::hiccup
  (spec/nonconforming
   (spec/or :ignore nil?
            :string string?
            :sequential seqable?
            :element (spec/cat :tag (spec/with-gen keyword? #(spec/gen #{:span :div}))
                               :attrs (spec/? map?)
                               :content (spec/* ::hiccup))
            :ui-element (spec/cat :fn ::maybe-fn
                                  :attrs (spec/? map?)
                                  :content (spec/* ::hiccup)))))
