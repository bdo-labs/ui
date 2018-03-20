(ns ui.element.period-picker.views
  (:require [ui.element.period-picker.spec :as spec]
            [ui.element.calendar.views :as calendar]
            [ui.element.containers.views :refer [container]]
            [ui.element.icon.views :refer [icon]]
            [ui.element.textfield.views :refer [textfield]]
            [ui.element.button.views :refer [button]]
            [ui.element.modal.views :refer [dialog]]
            [ui.util :as util]
            [ui.wire.polyglot :refer [translate]]
            [#?(:clj clj-time.format :cljs cljs-time.format) :as fmt]
            [#?(:clj clj-time.coerce :cljs cljs-time.coerce) :as coerce]
            [re-frame.core :as re-frame]))

(defn formatter [format inst]
  (when (inst? inst)
    (fmt/unparse (fmt/formatter format) (coerce/from-date inst))))

(defn display-formatter
  [format from to]
  (let [formatter (partial formatter format)]
    (str (formatter from) " - " (formatter to))))

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
        (util/conform! ::spec/params params)
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
