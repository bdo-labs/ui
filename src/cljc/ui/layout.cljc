(ns ui.layout
  "
  Layout

  A set of functions that will work for 99% of your layout-needs.
  "
  (:require [ui.elements :as element]
            #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [ui.util :as util]))

(spec/def ::aligned (spec/or :x ::horizontal-alignment
                             :y ::vertical-alignment))
(spec/def ::alignment #{:start :end :center})
(spec/def ::horizontal-alignment #{:left :right :center})
(spec/def ::vertical-alignment #{:top :bottom :middle})
(spec/def ::content-type (spec/or :nil nil? :fn fn? :seq seq? :str string? :vec vector?))
(spec/def ::variable-content (spec/* ::content-type))

(spec/def ::layout-params
  (spec/keys :opt-un [::aligned]))

(spec/def ::layout-args
  (spec/cat :params (spec/? ::layout-params)
            :content ::variable-content))


(defn- layout [layout & args]
  (let [{:keys [params content]} (util/conform-or-fail ::layout-args args)
        aligned                  (apply hash-map (-> params :aligned))
        align                    [(util/aligned->align (or (-> aligned :x) :left))
                                  (util/aligned->align (or (-> aligned :y) :top))]
        align                    (if (= layout :vertically) [(last align) (first align)] align)
        params                   (merge {:layout layout :align align} (dissoc params))]
    (apply element/container (into [params] (map last content)))))


(defn horizontally [& args]
  (apply layout :horizontally args))


(defn vertically [& args]
  (apply layout :vertically args))


(defn centered [& args]
  (let [{:keys [params content]} (util/conform-or-fail ::layout-args args)
        params                   (merge {:align [:center :center]} params)]
    (apply layout :horizontally params content)))

(defn fill [] [:span.fill])
