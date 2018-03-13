(ns ui.wire.feature
  (:require [re-frame.core :as re-frame]
            [ui.util :as util]
            [cemerick.url :as url]))

;; Events -----------------------------------------------------------------

(def feature-stack-interceptor
  [(re-frame/path :ui/feature-stack)])

(re-frame/reg-event-db
 :ui/set-feature
 feature-stack-interceptor
 (fn [stack [_ id value]]
   (assoc stack id (or value true))))

(re-frame/reg-event-db
 :ui/remove-feature
 feature-stack-interceptor
 (fn [stack [id]]
   (dissoc stack id)))

;; Subscriptions ----------------------------------------------------------

(re-frame/reg-sub
 :ui/feature-stack
 util/extract)

(re-frame/reg-sub
 :ui/feature
 :<- [:ui/feature-stack]
 (fn [stack [_ id]]
   (get stack id)))

;; User Functions ---------------------------------------------------------

(defn feature?
  [feature]
  "Predicate for checking a `feature`-toggle"
  @(re-frame/subscribe [:ui/feature feature]))
