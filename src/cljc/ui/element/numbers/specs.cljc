(ns ui.element.numbers.specs
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as str]))


(def numbers-only #"^[\d,\.]+$")


(spec/def ::name (spec/and string? not-empty))
(spec/def ::title
  (spec/and string?
            #(not (str/starts-with? % "http"))
            #(not (str/starts-with? % "="))
            #(nil? (re-matches numbers-only %))))
(spec/def ::titles (spec/coll-of ::title))
(spec/def ::title-row? boolean)
(spec/def ::column-heading #{:alpha :numeric :hidden})
(spec/def ::row-heading #{:alpha :numeric :select :hidden})
(spec/def ::type #{:number :inst :string :any})


(spec/def ::row-height integer?) ;; row-height in pixels
(spec/def ::num-visible-rows integer?)



(spec/def ::col-ref
  (spec/with-gen (spec/and keyword? #(re-matches #"[A-Z]+" (name %)))
                 #(spec/gen #{:A :ZA :ABA :ACMK :FOO})))


(spec/def ::cell-ref
  (spec/with-gen (spec/and keyword?
                           #(re-matches #"[A-Z]+[0-9]+" (str (name %))))
                 #(spec/gen #{:A1 :Z999 :AZ3 :BOBBY6 :ME2})))


(spec/def ::hide-column (spec/coll-of ::col-ref :into #{}))
(spec/def ::locked (spec/coll-of ::col-ref :into #{}))


#_(spec/def ::rows (spec/map-of ::cell-ref any?))
(spec/def ::rows (spec/coll-of map?))


(spec/def ::scale (spec/or :inst inst? :number number?))
(spec/def ::min ::scale)
(spec/def ::max ::scale)


(spec/def ::caption? boolean?)
(spec/def ::editable? boolean?)
(spec/def ::sortable? boolean?)
(spec/def ::filterable? boolean?)
(spec/def ::freeze? boolean?)
(spec/def ::hidden boolean?)


(spec/def ::inst-formatter fn?)
(spec/def ::number-formatter fn?)


(spec/def ::value (spec/and any?))
(spec/def ::values (spec/coll-of ::value))


(spec/def ::csv
  (spec/and (spec/coll-of vector?)
            (spec/cat :title-rows (spec/* ::titles)
                      :rows (spec/* ::values))))


(spec/def ::csv-no-title
  (spec/and (spec/coll-of vector?)
            (spec/cat :rows
                      (spec/* ::values))))


(spec/def ::unq-column (spec/coll-of map?))
(spec/def ::unq-columns (spec/coll-of ::unq-column))


(spec/def ::data
  (spec/or :data ::unq-columns :csv ::csv :csv-no-title ::csv-no-title))


(spec/def ::column
  (spec/keys :req-un [::type ::col-ref ::rows]
             :opt-un [::min ::max ::freeze? ::editable? ::sortable?
                      ::filterable? ::locked ::hide-column]))


(spec/def ::columns (spec/coll-of ::column))


(spec/def ::params
  (spec/keys :req-un [::name]
             :opt-un [::column-heading ::row-heading ::editable? ::caption?
                      ::hidden]))


(spec/def ::args
  (spec/cat :params ::params
            :content ::data))


(spec/fdef cell-ref
  :args (spec/cat :col-ref :col-ref
                  :num nat-int?)
  :ret keyword?)

