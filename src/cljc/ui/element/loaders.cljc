(ns ui.element.loaders
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [ui.util :as util]))

;; FIXME size & color

(defn spinner [& args]
  (let [{:keys [params]} (util/conform-or-fail ::spinner-args args)
        {:keys [color size]
         :or   {color "rgb(70,111,226)"
                size 1}} params]
    [:svg.Spinner {:view-box "0 0 50 50"
                   :width    "40px"
                   :height   "40px"
                   :style {:transform "scale(" size ")"}}
     [:path {:d     "M25.251,6.461c-10.318,0-18.683,8.365-18.683,18.683h4.068c0-8.071,6.543-14.615,14.615-14.615V6.461z"
             :fill (str (name color))}
      [:animateTransform {:attribute-name "transform"
                          :type           "rotate"
                          :from           "0 25 25"
                          :to             "360 25 25"
                          :dur            "0.6s"
                          :repeat-count   "indefinite"}]]]))


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
