(ns ui.layout
  (:require [ui.elements :as element]
            [clojure.spec.alpha :as spec]
            [ui.specs :as common]
            [ui.util :as util]))

;; Helper Functions -------------------------------------------------------

(defn aligned->align [v]
  (case v
    (:top :left)     :start
    (:bottom :right) :end
    :center))

;; Specification ----------------------------------------------------------

(spec/def ::aligned (spec/or :x ::horizontal-alignment
                             :y ::vertical-alignment))
(spec/def ::alignment #{:start :end :center})
(spec/def ::horizontal-alignment #{:left :right :center})
(spec/def ::vertical-alignment #{:top :bottom :middle})
(spec/def ::content-type
  (spec/nonconforming
   (spec/or :nil nil? :fn fn? :seq seq? :str string? :vec vector?)))
(spec/def ::variable-content (spec/* ::content-type))

(spec/def ::layout-params
  (spec/keys :opt-un [::aligned]))

(spec/def ::layout-args
  (spec/cat :params (spec/? ::layout-params)
            :content ::variable-content))

;; Views ------------------------------------------------------------------

(defn- layout [layout & args]
  (let [{:keys [params content]} (util/conform! ::layout-args args)
        aligned                  (apply hash-map (-> params :aligned))
        align                    [(aligned->align (or (-> aligned :x) :left))
                                  (aligned->align (or (-> aligned :y) :top))]
        align                    (if (= layout :vertically) [(last align) (first align)] align)
        params                   (merge {:layout layout :align align} (dissoc params))]
    (into [element/container params] content)))

(defn horizontally [& args]
  (apply layout :horizontally args))

(defn vertically [& args]
  (apply layout :vertically args))

(defn centered [& args]
  (let [{:keys [params content]} (util/conform! ::layout-args args)
        params                   (merge {:align [:center :center]} params)]
    (apply layout :horizontally params content)))

(defn fill [] [:span.fill])
