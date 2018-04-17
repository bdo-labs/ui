(ns ui.element.loaders.spec
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::rgb-space
  (spec/and nat-int? #(<= % 255)))

(spec/def ::alpha-space
  (spec/and number? #(>= % 0) #(<= % 1)))

(spec/def ::rgb
  (spec/cat :r ::rgb-space
            :g ::rgb-space
            :b ::rgb-space))

(spec/def ::rgba
  (spec/cat :r ::rgb-space
            :g ::rgb-space
            :b ::rgb-space
            :a ::alpha-space))

#_(spec/def ::hex
    (spec/and
     string?
     #(str/starts-with? % "#")))

(spec/def ::color-name
  #{:black
    :white
    :red
    :green
    :blue
    :yellow
    :lightgrey
    :lightgray
    :ghostwhite
   ;; etc..
})

(spec/def ::color
  (spec/nonconforming
   (spec/or
    :rgb ::rgb
    :rgba ::rgba
    ;;:hex ::hex
    :named ::color-name)))

(spec/def ::size (spec/and number? #(> % 0)))

(spec/def ::spinner-params
  (spec/keys :opt-un [::color
                      ::size]))

(spec/def ::spinner-args
  (spec/cat :params (spec/? ::spinner-params)))
