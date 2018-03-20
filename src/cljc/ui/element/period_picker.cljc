(ns ui.element.period-picker
  (:require [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [ui.element.calendar :as calendar]
            [ui.element.containers :refer [container]]
            [ui.element.icon :refer [icon]]
            [ui.element.textfield :refer [textfield]]
            [ui.element.button :refer [button]]
            [ui.element.modal :refer [dialog]]
            [ui.util :as util]
            [ui.wire.polyglot :refer [translate]]
            [#?(:clj clj-time.format :cljs cljs-time.format) :as fmt]
            [#?(:clj clj-time.coerce :cljs cljs-time.coerce) :as coerce]
            [re-frame.core :as re-frame]))

(spec/def ::stub
  (spec/with-gen fn?
    (gen/return (constantly nil))))

(spec/def ::id (spec/and string? #(not (= "" %))))
(spec/def ::from inst?)
(spec/def ::to inst?)
(spec/def ::min inst?)
(spec/def ::max inst?)
(spec/def ::start-of-week (spec/and integer? #(>= % 0) #(<= % 6)))
(spec/def ::formatter-args (spec/cat :format string? :time inst?))

(defn formatter [format inst]
  (when (inst? inst)
    (fmt/unparse (fmt/formatter format) (coerce/from-date inst))))
(spec/fdef formatter
           :args ::formatter-args
           :ret string?)

(defn display-formatter
  [format from to]
  (let [formatter (partial formatter format)]
    (str (formatter from) " - " (formatter to))))
(spec/fdef display-formatter
           :args (spec/cat :formatter ::formatter-args :from ::from :to ::to)
           :ret string?
           :fn #(< (:from (:args %)) (:to (:args %))))

(spec/def ::format
  (spec/with-gen string?
    #(spec/gen #{"E MMM d yyyy H:m" "E MMM d yyyy" "yyyyMMdd" "yyyyMMdd'T'HHmmss.SSSZ"
                 "yyyyMMdd'T'HHmmssZ yyyy-MM-dd'T'HH" "yyyy-MM-dd"})))
(spec/def ::time-picker boolean?)
(spec/def ::read-only boolean?)
(spec/def ::disabled boolean?)

;; (spec/def ::on-change ::stub)
;; (spec/def ::on-selected ::stub)


(spec/def ::params
  (spec/keys :req-un [::id]
             :opt-un [::from ::to ::min ::max ::start-of-week ::format
                      ::time-picker ::read-only ::disabled
                      ;; Listeners
                      ;; ::on-change ::on-selected
]))

(spec/def ::args
  (spec/cat :params ::params))

(re-frame/reg-event-db
 ::init
 (fn [db [_ id state]]
   (assoc-in db [:period-picker] (hash-map [id (merge {:show-picker false} state)]))))

(re-frame/reg-event-db
 ::toggle-show-picker
 (fn [db [_ id]]
   (update-in db [:period-picker id :show-picker] not)))

(re-frame/reg-sub
 ::show-picker
 (fn [db [_ id]]
   (get-in db [:period-picker id :show-picker])))

;; Period-picker interaction
;; Clicking the field when there's no period set, should bring up a
;; date-picker, otherwise it should enable free-editing of the period.
;; Clicking the calendar-icon should always bring up a date-picker It
;; must be easy to select the two dates, so having two calendar-months
;; up at a time could be benefitial

(defn period-picker
  [params]
  "### period-picker"
  (let [{:keys [id from to time-picker format]
         :or   {format "E MMM d yyyy H:m" #_(if time-picker "E MMM d yyyy H:m" "E MMM d yyyy")}}
        (util/conform! ::params params)
        show-picker        @(re-frame/subscribe [::show-picker id])
        toggle-show-picker #(re-frame/dispatch [::toggle-show-picker id])]
    (when (nil? show-picker)
      (re-frame/dispatch-sync [::init id params]))
    [container {:layout :vertically}
     [container {:layout :horizontally :fill? true}
      [textfield (merge {:placeholder (translate (if time-picker :ui/time-period :ui/date-period))}
                        (when (or (some? from) (some? to))
                          {:value (display-formatter format from to)}))]
      [button {:circular true :on-click toggle-show-picker :class "primary"}
       [icon {:font "ion"} "ios-calendar-outline"]]]
     [dialog {:show?     show-picker
              :on-cancel toggle-show-picker}
      [calendar/days {:short-form? true}]]]))
