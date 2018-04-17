(ns ui.element.ripple.views
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [clojure.spec.alpha :as spec]
            [clojure.core :refer [atom]]
            [#?(:clj clojure.core :cljs reagent.core) :as r]
            [clojure.core.async :refer [<! timeout #?(:clj go)]]
            [ui.util :as util]))


(spec/def ::ripple boolean?)


(spec/def ::params
  (spec/keys :opt-un [::ripple]))


(spec/def ::args
  (spec/cat :params ::params))


(defn ripple [& args]
  (let [{:keys [params]} (util/conform! ::args args)
        {:keys [ripple]} params
        element*         (atom nil)
        coord*           (r/atom {:r 10 :cy 1 :cx 1})
        classes*         (r/atom "")]
    (fn []
      (letfn [(on-click [event]
                (when-let [element @element*]
                  (let [dimensions (.getBoundingClientRect (.-parentElement element))
                        mouse      {:x (- (-> event .-clientX) (aget dimensions "x"))
                                    :y (- (-> event .-clientY) (aget dimensions "y"))}]
                    (reset! classes* "animate")
                    (reset! coord* {:r 10 :cx (:x mouse) :cy (:y mouse)})
                    (go (<! (timeout 500))
                        (reset! classes* "")))))]
        [:svg.Ripple (merge {:ref       #(reset! element* %)
                             :class     @classes*
                             :view-box   "0 0 100 100"
                             :focusable "false"}
                            (when (not (false? ripple))
                              {:on-click on-click}))
         (when (not (false? ripple))
           [:circle @coord*])]))))
