(ns ui.wire.form.helpers
  (:require [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.wire.polyglot :refer [translate]]
            [ui.util :as util]))

(defn valid?
  "Helper function for checking validity of the sub ::on-valid. Accepts a form, subscription or value"
  [v]
  (cond (record? v)
        (not= @(re-frame/subscribe [:ui.wire.form/on-valid (:id v)]) :ui.wire.form/invalid)

        (util/deref? v)
        (not= @v :ui.wire.form/invalid)

        :else
        (not= v :ui.wire.form/invalid)))

(defn wizard?
  "Is the form a wizard?"
  [form]
  (= :wizard (get-in form [:options :render])))

(defn re-render?
  "Does the form need to be re-rendered (e.g., a wizard?)"
  [form]
  (#{:wizard} (get-in form [:options :render])))


(defn button [data finished-fn]
  (let [-valid? (valid? data)]
    [element/button {:class "primary"
                     :disabled (not -valid?)
                     :on-click #(when (and -valid? (fn? finished-fn))
                                  (finished-fn data))}
     (translate :ui.wire.form/done)]))

(defn table-button [form-map finished-fn]
  (let [on-valid (re-frame/subscribe [:ui.wire.form/on-valid (:id form-map)])]
    (fn [] [:tr [:td] [:td [button @on-valid finished-fn]]])))
(defn list-button [form-map finished-fn]
  (let [on-valid (re-frame/subscribe [:ui.wire.form/on-valid (:id form-map)])]
    (fn [] [:li [button @on-valid finished-fn]])))
(defn paragraph-button [form-map finished-fn]
  (let [on-valid (re-frame/subscribe [:ui.wire.form/on-valid (:id form-map)])]
    (fn [] [:p [button @on-valid finished-fn]])))
(defn template-button [form-map finished-fn]
  (let [on-valid (re-frame/subscribe [:ui.wire.form/on-valid (:id form-map)])]
    (fn [] [button @on-valid finished-fn])))
