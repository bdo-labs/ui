(ns ui.element.textfield.views
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [ui.util :as util]
            [ui.element.textfield.spec :as spec]
            [clojure.string :as str]))

(defn textfield
  "### textfield


  "
  [{:keys [id model value change-on-blur on-change on-blur on-key-up on-focus] :or {id (util/gen-id)}}]
  (let [initial-value (if (util/deref? model) @model (str value))
        model         (if (util/deref? model) model (atom (str value)))
        --on-change   #(let [value (-> % .-target .-value)]
                         (when (not= value @model)
                           (reset! model value)
                           (when (ifn? on-change) (on-change @model %))))
        --on-focus    #(when-let [value (-> % .-target .-value)]
                         (when (ifn? on-focus) (on-focus value %)))
        --on-blur     #(when-let [value (-> % .-target .-value)]
                         (when change-on-blur (--on-change %))
                         (when (ifn? on-blur) (on-blur value %)))
        --on-key-up   #(let [k     (util/code->key (-> % .-which))
                             value (-> % .-target .-value)]
                         (if (ifn? on-key-up)
                           (on-key-up k value %)
                           (case k
                             "enter" (--on-change %)
                             "esc"   (reset! model initial-value)
                             true)))]
    (fn [& args]
      (let [{:keys [params]} (util/conform! ::spec/args args)
            {:keys [style]}  params
            ui-params        (select-keys params (util/keys-from-spec ::spec/params))
            class            (str/join " " [(util/params->classes ui-params)
                                            (when (seq @model) "not-empty")
                                            (when (not= initial-value @model) "dirty")])]
        [:div.Textfield {:key (util/slug "textfield" id) :style style :class class}
         [:input (merge (dissoc ui-params :label :model)
                        {:type          :text
                         :value         @model
                         :auto-complete "off"
                         :on-change     --on-change
                         :on-blur       --on-blur
                         :on-focus      --on-focus
                         :on-key-up     --on-key-up})]
         (when-let [label (:label params)]
           [:label {:for id} label])]))))
