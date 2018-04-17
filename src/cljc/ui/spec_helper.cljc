(ns ui.spec-helper
  "Brian Noguchi - https://github.com/lab-79/clojure-spec-helpers "
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen :refer [generator?]]))

(s/def ::spec-name (s/and keyword? #(contains? (s/registry) %)))

(s/def ::desc (s/or :spec-name ::spec-name
                    :keys-form ::keys-form
                    :coll-form ::coll-form
                    :and-form ::and-form
                    :merge-desc ::merge-desc
                    :or-desc ::or-desc
                    :quoted-fn ::quoted-fn))

(s/def ::keys-form (s/cat :macro #{'keys}
                          :args (s/keys* :opt-un [::req ::opt])))

(s/def ::req (s/coll-of ::spec-name :kind vector?))

(s/def ::opt (s/coll-of ::spec-name :kind vector?))

(s/def ::coll-form (s/cat :macro #{'coll-of}
                          :member-type (s/alt :spec-name ::spec-name
                                              :spec-form list?
                                              :pred symbol?
                                              :set set?)
                          :rest (s/* any?)))

(s/def ::and-form (s/cat :macro #{'and}
                         :rest (s/+ (s/or :spec-name ::spec-name
                                          :keys-form ::keys-form
                                          :merge-desc ::merge-desc
                                          :pred symbol?
                                          :quoted-fn ::quoted-fn
                                          :comp-fn (s/cat :comp #{'comp}
                                                          :rest (s/+ any?))))))

(s/def ::merge-desc (s/cat :macro #{'merge}
                           :rest (s/+ (s/or :spec-name keyword?
                                            :spec-desc list?))))

(s/def ::or-desc (s/cat :macro #{'or}
                        :rest (s/+ (s/cat :tag keyword?
                                          :spec ::desc))))

(s/def ::quoted-fn (s/cat :fn #{'fn}
                          :rest (s/+ any?)))

(s/def ::gen-overrides (s/? (s/map-of ::spec-name
                                      ::gfn)))
(s/def ::gfn (s/fspec :args (s/cat)
                      :ret generator?
                      :fn #(empty? (:args %))))

(declare extract-spec-keys)

(s/fdef spec->spec-keys
        :args (s/cat :spec seq?)
        :ret (s/keys :opt-un [::req ::opt]))
; TODO We don't have a nice way to catch when a :req or :opt key is missing in this fn
(defn spec->spec-keys
  [spec-form]
  (condp s/valid? spec-form
    ::keys-form
    (let [out (s/conform ::keys-form spec-form)]
      (:args out))

    ::coll-form
    (let [out (s/conform ::coll-form spec-form)
          member-type (:member-type out)]
      (condp = (first member-type)
        :spec-name (extract-spec-keys (second member-type))
        :spec-form (spec->spec-keys (second member-type))
        ; else
        (throw (ex-info "The spec should generate a map or collection of maps."
                        {:spec spec-form}))))

    ::and-form
    (let [out (s/conform ::and-form spec-form)
          rest (-> out :rest)]
      (reduce (fn [spec-keys [type conformed]]
                (condp = type
                  :spec-name (let [{:keys [req opt]} (spec->spec-keys (s/describe conformed))]
                               (-> spec-keys
                                   (update :req into req)
                                   (update :opt into opt)))
                  :keys-form (let [{:keys [req opt]} (:args conformed)]
                               (-> spec-keys
                                   (update :req into req)
                                   (update :opt into opt)))
                  :merge-desc (let [{:keys [req opt]}
                                    (spec->spec-keys (s/unform ::merge-desc
                                                               conformed))]
                                (-> spec-keys
                                    (update :req into req)
                                    (update :opt into opt)))
                  :quoted-fn spec-keys
                  :comp-fn spec-keys
                  :pred spec-keys))
              {:req [] :opt []}
              rest))

    ::or-desc
    (let [out (s/conform ::or-desc spec-form)
          rest (:rest out)]
      (->> rest
           (remove (fn [{[type _] :spec}] (= :quoted-fn type)))
           ((fn [x]
              (if (empty? x)
                (throw (ex-info "The spec should generate a map or collection of maps."
                                {:spec spec-form}))
                x)))
           (reduce
            (fn [spec-keys {[type spec-name-or-form] :spec}]
              (let [{:keys [req opt]}
                    (condp = type
                      :spec-name (extract-spec-keys spec-name-or-form)
                      (let [matching-spec (condp = type
                                            :keys-form ::keys-form
                                            :coll-form ::coll-form
                                            :and-form ::and-form
                                            :merge-desc ::merge-desc)]
                        (spec->spec-keys (s/unform matching-spec spec-name-or-form))))]
                (-> spec-keys
                    (update :req into req)
                    (update :opt into opt))))
            {:req [] :opt []})))

    ::merge-desc
    (let [out (s/conform ::merge-desc spec-form)
          rest (:rest out)]
      (->> rest
           (filter (fn [[_ spec-name-or-form]]
                     (not (s/valid? ::quoted-fn spec-name-or-form))))
           (map (fn [[type spec-name-or-form]]
                  (condp = type
                    :spec-name (extract-spec-keys spec-name-or-form)
                    :spec-desc (spec->spec-keys spec-name-or-form))))
           ((fn [x]
              (if (empty? x)
                (throw (ex-info "The spec should generate a map or collection of maps."
                                {:spec spec-form}))
                x)))
           (apply merge-with into)))

    ;else
    (do
      (if (= 'keys (first spec-form))
        (throw (ex-info "Keys spec fails"
                        {:spec-form spec-form
                         :explain-data (s/explain-data ::keys-form spec-form)})))
      (throw (ex-info "The spec should generate a map or collection of maps."
                      {:spec spec-form})))))

(s/fdef extract-spec-keys
        :args (s/cat :spec-name ::spec-name)
        :ret (s/keys :opt-un [::req ::opt]))
(defn extract-spec-keys
  [spec-name]
  (spec->spec-keys (s/describe spec-name)))

(s/fdef is-keys-spec?
        :args (s/cat :spec-name ::spec-name)
        :ret boolean?)
(defn is-keys-spec?
  [spec-name]
  (let [spec (s/describe spec-name)]
    (and (coll? spec)
         (condp = (first spec)
           'keys true
           'coll-of (and (s/valid? ::spec-name (second spec))
                         (is-keys-spec? (second spec)))
           'every   (and (s/valid? ::spec-name (second spec))
                         (is-keys-spec? (second spec)))
           'and     (and (s/valid? ::spec-name (second spec))
                         (is-keys-spec? (second spec)))
           'merge   (and (s/valid? ::spec-name (second spec))
                         (is-keys-spec? (second spec)))
           false))))

(s/fdef generates-map-or-coll-of-maps?
        :args (s/cat :spec-name ::spec-name
                     :gen-overrides ::gen-overrides)
        :ret boolean?)
(defn generates-map-or-coll-of-maps?
  [spec-name gen-overrides]
  (let [data (gen/generate (s/gen spec-name gen-overrides))]
    (or (map? data)
        (and (coll? data)
             ; s/every with no :min-count may generate an empty coll, so
             ; make sure we generate a coll with at least cardinality 1
             (let [coll-with-1+ (gen/generate (s/gen (s/and spec-name
                                                            seq)
                                                     gen-overrides))]
               (map? (first coll-with-1+)))))))

(s/fdef is-collection-spec?
        :args (s/cat :spec-name ::spec-name)
        :ret boolean?)
(defn is-collection-spec? [spec-name]
  (s/valid? ::coll-form (s/describe spec-name)))
