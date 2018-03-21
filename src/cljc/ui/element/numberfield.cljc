(ns ui.element.numberfield
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [clojure.test.check.generators :as gen]
            [ui.util :as util]))


(spec/def ::maybe-fn
  (spec/with-gen fn?
    (gen/return (constantly nil))))


;; Events
(spec/def ::on-change ::maybe-fn)
(spec/def ::on-focus ::maybe-fn)
(spec/def ::on-blur ::maybe-fn)
(spec/def ::on-key-up ::maybe-fn)
(spec/def ::on-key-down ::maybe-fn)


;; Parameters
(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))
(spec/def ::placeholder #(or (string? %) (nil? %)))
(spec/def ::max (spec/or :number number? :nil nil?))
(spec/def ::min (spec/or :number number? :nil nil?))
(spec/def ::step (spec/or :number number? :nil nil?))
(spec/def ::label (spec/or :string string? :number number?))
(spec/def ::disabled boolean?)
(spec/def ::auto-focus boolean?)
(spec/def ::read-only boolean?)
(spec/def ::focus boolean?)
(spec/def ::model util/deref?)
(spec/def ::--params
  (spec/keys :opt-un [::id
                      ::placeholder
                      ::auto-focus
                      ::label
                      ::disabled
                      ::read-only
                      ::focus
                      ::max
                      ::min
                      ::step
                      ::on-change
                      ::on-focus
                      ::on-blur
                      ::on-key-up
                      ::on-key-down]))

(spec/def ::params
  (spec/merge ::--params
              (spec/keys :opt-un [::model])))


(spec/def ::args (spec/cat :params ::params))

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
  (let [{:keys [params]}            (util/conform! ::args args)
        {:keys [id on-change model]
         :or   {id (util/gen-id)}}  params
        on-change'                  (if on-change
                                      (fn [e]
                                        (handle-on-change model params)
                                        (on-change e))
                                      (handle-on-change model params))]
    (fn [& args]
      (let [{:keys [params]}            (util/conform! ::args args)
            {:keys [style
                    placeholder
                    label]
             :or   {style       {}
                    placeholder ""}} params
            ui-params                   (select-keys params (util/keys-from-spec ::--params))
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
