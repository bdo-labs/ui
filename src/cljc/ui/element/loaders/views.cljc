(ns ui.element.loaders.views
  (:require [ui.element.loaders.spec :as spec]
            [clojure.string :as str]
            [ui.util :as util]))

;; FIXME size & color

(defn spinner [& args]
  (let [{:keys [params]} (util/conform! ::spec/spinner-args args)
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

