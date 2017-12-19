(ns ui.wire.load
  (:require [re-frame.core :as re-frame]
            [ui.util :as util]))


;; Events -----------------------------------------------------------------


(def load-stack-interceptor
  [(re-frame/path :ui/load-stack)])


(re-frame/reg-event-db
 :ui/set-load
 load-stack-interceptor
 (fn [stack [_ id value]]
   (assoc stack id (or value true))))


(re-frame/reg-event-db
 :ui/remove-load
 load-stack-interceptor
 (fn [stack [id]]
   (dissoc stack id)))


;; Effects ----------------------------------------------------------------


(re-frame/reg-fx
 :set-load
 (fn [id value]
   (re-frame/dispatch [:ui/set-load id value])))


(re-frame/reg-fx
 :remove-load
 (fn [id value]
   (re-frame/dispatch [:ui/remove-load id value])))


;; Subscriptions ----------------------------------------------------------


(re-frame/reg-sub
 :ui/load-stack
 util/extract)


(re-frame/reg-sub
 :ui/loading
 :<- [:ui/load-stack]
 (fn [stack]
   (->> stack
      (filter #(or (true? %) (number? %)))
      (seq))))


(re-frame/reg-sub
 :ui/load
 :<- [:ui/load-stack]
 (fn [stack [_ id]]
   (get stack id)))
