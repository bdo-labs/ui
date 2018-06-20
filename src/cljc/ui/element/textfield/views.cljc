(ns ui.element.textfield.views
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.core.async :refer [<! timeout #?(:clj go)]]
            [ui.util :as util]
            [ui.element.textfield.spec :as spec]
            [ui.element.button.views :refer [button]]
            [ui.element.icon.views :refer [icon]]
            [clojure.string :as str]))

(defn textfield
  "### textfield


  "
  [{:keys [id
           model
           value
           required
           valid?
           change-on-blur
           on-cancel
           on-change
           on-blur
           on-key-up
           on-focus]
    :or   {id       (util/gen-id)
           valid?   seq
           required false}}]
  (let [initial-value (if (util/ratom? model) @model (str value))
        model         (if (util/ratom? model) model (atom (str value)))
        focused*      (atom false)
        invalid*      (atom false)
        dirty*        (atom false)
        --on-cancel   #(do (reset! model "")
                           (when (ifn? on-change) (on-change @model %))
                           (when (ifn? on-cancel) (on-cancel %)))
        --on-change   #(let [value (-> % .-target .-value)]
                         (when (not= value @model)
                           (reset! model value)
                           (when (ifn? on-change) (on-change @model %))))
        --on-focus    #(do (reset! focused* true)
                           (when-let [value (-> % .-target .-value)]
                             (when (ifn? on-focus) (on-focus value %))))
        --on-blur     #(do (.persist %)
                           (go (<! (timeout 150))
                               (reset! focused* false)
                               (when-let [value (-> % .-target .-value)]
                                 (when change-on-blur (--on-change %))
                                 (when (ifn? on-blur) (on-blur value %)))))
        --on-key-up   #(let [k     (util/code->key (-> % .-which))
                             value (-> % .-target .-value)]
                         (if (ifn? on-key-up)
                           (on-key-up k value %)
                           (case k
                             "enter" (--on-change %)
                             "esc"   (reset! model initial-value)
                             true)))]
    (fn [& args]
      (let [{:keys [params]}      (util/conform! ::spec/args args)
            {:keys [value style]} params
            ;; Use the value upon change
            ui-params             (select-keys params (util/keys-from-spec ::spec/params))
            class                 (str/join " " [(util/params->classes ui-params)
                                                 (when (seq @model) "not-empty")
                                                 (when @focused* "focus")
                                                 (when required
                                                   (if @invalid* "invalid" "valid"))
                                                 (when @dirty* "dirty")])]
        (when (not= initial-value @model) (reset! dirty* true))
        (when (and required @dirty*)
          (reset! invalid* (and required ((complement valid?) @model))))
        [:div.Textfield {:key (util/slug "textfield" id) :style style :class class}
         [:input (merge {:type :text}
                        (dissoc ui-params :label :model :valid?)
                        {:value         @model
                         :auto-complete "off"
                         :on-change     --on-change
                         :on-blur       --on-blur
                         :on-focus      --on-focus
                         :on-key-up     --on-key-up})]
         (when (and (= (:type ui-params) :search)
                    (seq @model)
                    @focused*)
           [button {:style    {:position         :absolute
                               :right            0
                               :z-index          100
                               :transform-origin :right
                               :transform        "scale(0.5) translateY(-0.5em)"}
                    :on-click --on-cancel
                    :circular true} [icon {} "close"]])
         (when-let [label (:label params)]
           [:label {:for id} label])]))))
