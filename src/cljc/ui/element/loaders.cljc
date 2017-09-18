(ns ui.element.loaders
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [garden.units :as unit]))


(defcssfn scale)


(defn style [{:keys [primary]}]
  [[:.Spinner
    [:path {:fill primary}]]])


(defn spinner []
  [:svg.Spinner {:view-box "0 0 50 50"
                 :width "40px"
                 :height "40px"}
   [:path {:d "M25.251,6.461c-10.318,0-18.683,8.365-18.683,18.683h4.068c0-8.071,6.543-14.615,14.615-14.615V6.461z"}
    [:animateTransform {:attribute-name "transform"
                        :type "rotate"
                        :from "0 25 25"
                        :to "360 25 25"
                        :dur "0.6s"
                        :repeat-count "indefinite"}]]])
