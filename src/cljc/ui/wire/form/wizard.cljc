(ns ui.wire.form.wizard
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.specs :as specs.common]
            [ui.wire.form.common :as common]
            [ui.wire.form.helpers :as helpers]
            [ui.wire.polyglot :refer [translate]]
            [ui.util :as util]))


(defn show-form-fn
  "Show the form with the fn chosen, can send in a new field-ks"
  ([form-fn args]
   (condp = (count args)
     0 [form-fn]
     1 [form-fn (nth args 0)]
     2 [form-fn (nth args 0) (nth args 1)]
     [form-fn (nth args 0) (nth args 1) (nth args 2)]))
  ([form-fn field-ks current-step args]
   (let [[params form-map & r-args] args
         new-args (into [params (-> form-map
                                    (assoc :field-ks field-ks)
                                    (assoc-in [:options :wizard :current-step] current-step)) ] r-args)]
     (show-form-fn form-fn new-args))))

(defn render-navigation [{:keys [step last-step? first-step? max-steps form-map]}]
  (let [data @(re-frame/subscribe [:ui.wire.form/on-valid (:id form-map)])
        valid-fn (get-in form-map [:options :wizard :valid-fn])
        valid? (helpers/valid? data)]
    [:div.Pagination
     [:div.Prev
      [element/button {:disabled first-step?
                       :class "primary"
                       :on-click #(reset! step (max 0 (dec @step)))} (translate ::prev)]]
     [:div.Next
      [element/button {:disabled (and last-step? (not valid?))
                       :class "primary"
                       :on-click #(cond (not last-step?)
                                        (reset! step (min max-steps (inc @step)))

                                        (and last-step? valid? valid-fn)
                                        (valid-fn data)

                                        :else
                                        nil)}
       (if (and last-step? valid?)
         (translate :ui.wire.form/done)
         (translate ::next))]]]))

(defn render-step [{:keys [step step-opts max-steps style-map]} form-fn {:keys [id] :as form-map} args]
  (let [{:keys [fields legend]} step-opts
        first-step? (zero? @step)
        last-step? (= (dec max-steps) @step)
        render-navigation (or (get-in form-map [:options :wizard :render-navigation]) render-navigation)]
    [:div.Wizard (merge style-map {:key (str "wizard-" id)})
     [:div.Legend legend]
     (show-form-fn form-fn fields @step args)
     (render-navigation {:step step
                         :first-step? first-step?
                         :last-step? last-step?
                         :form-map form-map
                         :max-steps max-steps})]))

(defn run-wizard [form-fn args]
  (let [step (atom 0)
        [_ form-map] args
        default-style-map {:min-height (as-> (get-in form-map [:options :wizard :steps]) $
                                         (map count $)
                                         (apply max $)
                                         (str (* 10 $) "em"))}
        max-steps (count (get-in form-map [:options :wizard :steps]))]
    (fn [form-fn args]
      (let [[params form-map] args
            step-opts (get-in form-map [:options :wizard :steps @step])
            style-map {:style (merge default-style-map
                                     (get-in form-map [:options :wizard :style])
                                     (get-in params [:wizard :style])
                                     (get-in step-opts [:style]))}]
        (re-frame/dispatch [:ui.wire.form/wizard-current-step (:id form-map) @step])
        (render-step {:step step :step-opts step-opts :style-map style-map :max-steps max-steps}
                     form-fn form-map args)))))

(defn wizard
  "Takes a form-fn (as-table, as-list, etc) and puts into a surrounding wizard if the form is to be rendered as a wizard"
  [form-fn]
  (fn [& args]
    (let [[_ form-map] args
          wizard? (= :wizard (get-in form-map [:options :render]))]
      (fn [& args]
        (if wizard?
          [run-wizard form-fn args]
          (show-form-fn form-fn args))))))
