(ns ui.element.date-picker
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [#?(:clj clj-time.coerce :cljs cljs-time.coerce) :as coerce]
            [#?(:clj clj-time.core :cljs cljs-time.core) :as t]
            [#?(:clj clj-time.format :cljs cljs-time.format) :as fmt]
            [re-frame.core :as re-frame]
            [clojure.core.async :as async :refer [<! #?(:clj go) timeout]]
            [ui.element.calendar :refer [calendar]]
            [ui.element.textfield :refer [textfield]]
            [ui.util :as u]))


(re-frame/reg-event-db ::set-query (fn [db [_ dt]] (assoc db ::set-query dt)))
(re-frame/reg-event-db ::set-open (fn [db [_ open?]] (assoc db ::open? open?)))
(re-frame/reg-sub ::open? (fn [db] ^boolean (::open? db)))
(re-frame/reg-sub ::query (fn [db [_ dt]] (or (::query db) dt)))


;; TODO Enable free-editing of text-field
;; TODO Make handlers and subscriptions unique to each picker
(defn date-picker
  [{:keys [on-click on-focus on-navigation value nav? calendar? placeholder selectable? weekend short-form? jump]
    :or   {nav?        true
           calendar?   true
           value       (t/now)
           placeholder "Select a date"
           selectable? #(true? true)
           weekend     true}
    :as   params}]
  (let [open?             @(re-frame/subscribe [::open?])
        query             @(re-frame/subscribe [::query value])
        minimum           (:min params)
        maximum           (:max params)
        on-internal-click #(do (re-frame/dispatch [::set-query %])
                               (when (fn? on-click) (on-click %)))]
    [:div.Date-picker
     [textfield {:placeholder placeholder
                 :value       (fmt/unparse (fmt/formatter "dd. MMM. yyyy") query)
                 :on-focus    #(do
                                 (re-frame/dispatch [::set-open true])
                                 (when (fn? on-focus) (on-focus %)))
                 :on-blur     #(let [target (.-target %)]
                                 #?(:cljs (go (<! (timeout 250))
                                              (let [has-focus? ^boolean (= target (.-activeElement js/document))]
                                                (when (and open? (not has-focus?))
                                                  (re-frame/dispatch [::set-open false]))))))}]
     (when (and open? calendar?)
       [calendar {:value       query
                  :on-click    on-internal-click
                  :selectable? selectable?
                  :short-form? short-form?
                  :placeholder placeholder
                  :weekend     weekend
                  :on-navigation on-navigation
                  :jump        jump
                  :nav?        nav?
                  :min         minimum
                  :max         maximum}])]))
