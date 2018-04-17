(ns ui.element.numberfield.views
  (:require [clojure.string :as str]
            [clojure.test.check.generators :as gen]
            [ui.element.numberfield.spec :as spec]
            [ui.util :as util]))

(defn- handle-on-change [model {:keys [min max]}]
  #?(:cljs
     (let [-min (or min (* -1 ##Inf))
           -max (or max ##Inf)]
      (fn [e]
        (try
          (let [target-value (-> e .-target .-value)
                v (js/parseFloat target-value)]
            (cond (str/blank? target-value) (reset! model nil)

                  (and (not (js/isNaN v))
                       (>= v -min)
                       (<= v -max))         (reset! model v)

                  ;; do nothing
                  :else                     nil))
          (catch #?(:cljs js/Error) #?(:clj Exception) ex
                 (util/log ex)
                 ;; Do nothing. This is important as we don't want to lose the previous value
                 ))))))

(defn numberfield
  [& args]
  (let [{:keys [params]}            (util/conform! ::spec/args args)
        {:keys [id on-change model]
         :or   {id (util/gen-id)}}  params
        on-change'                  (if on-change
                                      (fn [e]
                                        (handle-on-change model params)
                                        (on-change e))
                                      (handle-on-change model params))]
    (fn [& args]
      (let [{:keys [params]}            (util/conform! ::spec/args args)
            {:keys [style
                    placeholder
                    label]
             :or   {style       {}
                    placeholder ""}} params
            ui-params                   (select-keys params (util/keys-from-spec ::spec/--params))
            class                       (str/join " " [(util/params->classes ui-params)
                                                       (when (or (nil? @model)
                                                                 (not (empty? placeholder))) "dirty")])]
        [:div.Numberfield {:key   (util/slug "numberfield" id)
                           :style style
                           :class class}
         [:input (merge
                  (dissoc ui-params :placeholder :label)
                  {:value         (or (do @model) "")
                   :type          :number
                   :placeholder   placeholder
                   :on-change     on-change'
                   :auto-complete "off"})]
         (when-not (empty? label)
           [:label {:for id} label])]))))
