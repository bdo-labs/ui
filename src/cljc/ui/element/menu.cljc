(ns ui.element.menu
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [ui.util :as util]
            [clojure.string :as str]
            [clojure.spec.alpha :as spec]
            [ui.element.containers :refer [container]]))


;; FIXME Close drop-down upon clicking outside the menu or holding the mouse outside for a longer period of time


(defcssfn cubic-bezier)
(defcssfn translateY)
(defcssfn scale)


;; TODO These !important rules should be avoided
(defn style [theme]
  [[:.Dropdown {:position         [[:absolute :!important]]
                :background       :white
                :transform        (scale 1)
                :transform-origin [[:top :right]]
                ;; Transition has been temporarily removed due to performance-issues
                ;; :transition       [[:200ms (cubic-bezier 0.770, 0.000, 0.175, 1.000)]]
                :z-index          90
                :font-weight      :normal}
    [:&.not-open {:transform (scale 0)}]
    [:&.origin-top-left {:transform-origin [[:top :left]]}]
    [:&.origin-top-right {:transform-origin [[:top :right]]}]
    [:&.origin-top-center {:transform-origin [[:top :center]]}]
    [:&.origin-bottom-left {:transform-origin [[:bottom :left]]}]
    [:&.origin-bottom-right {:transform-origin [[:bottom :right]]}]
    [:&.origin-bottom-center {:transform-origin [[:bottom :center]]}]]])


(spec/def ::open? boolean?)
(spec/def ::content-type (spec/or :nil nil? :seq seq? :str string? :vec vector?))
(spec/def ::variable-content (spec/* ::content-type))
(spec/def ::origins #{:top :bottom :left :right :center})
(spec/def ::origin
  (spec/coll-of ::origins :count 2))


(spec/def ::dropdown-params
  (spec/keys :req-un [::open?]
             :opt-un [::origin]))


(spec/def ::dropdown-args
  (spec/cat :params ::dropdown-params
            :content ::variable-content))


(defn dropdown [& args]
  (let [{:keys [params content]}   (util/conform! ::dropdown-args args)
        {:keys [id
                open?
                origin]
         :or   {id (util/gen-id)}} params
        classes                    (str "Dropdown "
                                        (if open? "open " "not-open ")
                                        (when origin (str "origin-" (str/join "-" (map name origin)))))
        ui-params                  (util/keys-from-spec ::dropdown-params)
        container-params           {:key      (util/slug id "dropdown")
                                    :layout   :vertically
                                    :gap?     false
                                    :raised?  true
                                    :rounded? true
                                    :class    classes}
        params                     (merge container-params (apply dissoc params ui-params))]
    (into [container params]
          (when open? (map last content)))))

