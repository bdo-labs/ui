(ns ui.element.numberfield.views
  (:require [clojure.string :as str]
            [clojure.test.check.generators :as gen]
            [ui.element.numberfield.spec :as spec]
            [ui.util :as util]))

(defn- handle-input [model e]
  #?(:cljs
     (try
       (let [target-value (-> e .-target .-value)
             v (js/parseFloat target-value)]
         (when-not (= v @model)
           (cond (str/blank? target-value) (reset! model nil)

                 ;; we're all good? change the model
                 (not (js/isNaN v))
                 (reset! model v)

                 ;; do nothing
                 :else                     nil)))
       (catch #?(:cljs js/Error) #?(:clj Exception) ex
              (util/log ex)
              ;; Do nothing. This is important as we don't want to lose the previous value
              ))))

(defn- get-event-handler [event-handler model]
  (fn [e] (do (handle-input model e)
              (when (ifn? event-handler) (event-handler @model e)))))

(defn numberfield
  [& args]
  (let [{:keys [params]}            (util/conform! ::spec/args args)
        {:keys [id on-change on-blur on-key-up on-focus on-key-down model]
         :or   {id (util/gen-id)}}  params
        --on-change (get-event-handler on-change model)
        --on-blur   (get-event-handler on-blur model)
        --on-focus  (get-event-handler on-focus model)
        --on-key-up (get-event-handler on-key-up model)
        --on-key-down #(when (ifn? on-key-down) (on-key-down @model %))]
    (fn [& args]
      (let [{:keys [params]}            (util/conform! ::spec/args args)
            {:keys [style placeholder label]
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
                  {:value         (or @model "")
                   :type          :number
                   :placeholder   placeholder
                   :on-change     --on-change
                   :on-blur       --on-blur
                   :on-focus      --on-focus
                   :on-key-up     --on-key-up
                   :on-key-down   --on-key-down
                   :auto-complete "off"})]
         (when-not (empty? label)
           [:label {:for id} label])]))))
