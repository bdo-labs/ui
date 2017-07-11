(ns ui.element.menu
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [ui.util :as u]
            [clojure.string :as str]
            [clojure.spec :as spec]
            [garden.units :as unit]
            [garden.color :as color]
            [ui.element.containers :refer [container]]))


(defcssfn cubic-bezier)
(defcssfn translateY)
(defcssfn scale)


(defn style [theme]
  [[:.Dropdown {:z-index          100
                :transform        (scale 1)
                :transform-origin [[:top :right]]
                :transition       [[:200ms (cubic-bezier 0.770, 0.000, 0.175, 1.000)]]}
    [:&.not-open {:transform (scale 0)}]
    [:&.origin-top-left {:transform-origin [[:top :left]]}]
    [:&.origin-top-right {:transform-origin [[:top :right]]}]
    [:&.origin-top-center {:transform-origin [[:top :center]]}]
    [:&.origin-bottom-left {:transform-origin [[:bottom :left]]}]
    [:&.origin-bottom-right {:transform-origin [[:bottom :right]]}]
    [:&.origin-bottom-center {:transform-origin [[:bottom :center]]}]
    ]])


(spec/def ::open? boolean?)
(spec/def ::variable-content (spec/* (or vector? string?)))
(spec/def ::origins #{:top :bottom :left :right :center})

(spec/def ::origin
  (spec/coll-of ::origins :count 2))

(spec/def ::dropdown-params
  (spec/keys :req-un [::open?]))


(spec/def ::dropdown-args
  (spec/cat :params ::dropdown-params
            :content ::variable-content))


(defn dropdown [params & args]
  (let [{:keys [params content]} (u/conform-or-fail ::dropdown-args (into [params] (vec args)))
        ui-params                (mapv (comp keyword name) (last (spec/form ::dropdown-params)))
        {:keys [open? origin]}          params
        params                   (merge {:layout   :vertically
                                         :gap?     false
                                         :raised?  true
                                         :rounded? true
                                         :class    (str "Dropdown "
                                                        (if open? "open " "not-open ")
                                                        (when origin (str "origin-" (str/join origin))))} (apply dissoc params ui-params))]
    (into [container params] content)))
