(ns ui.element.textfield
  (:require [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [ui.util :as util]
            [clojure.string :as str]))

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
(spec/def ::label string?)
(spec/def ::value string?)
(spec/def ::disabled boolean?)
(spec/def ::auto-focus boolean?)
(spec/def ::read-only boolean?)
(spec/def ::focus boolean?)
(spec/def ::--params
  (spec/keys :opt-un [::id
                      ::placeholder
                      ::auto-focus
                      ::label
                      ::disabled
                      ::read-only
                      ::focus
                      ::on-change
                      ::on-focus
                      ::on-blur
                      ::on-key-up
                      ::on-key-down]))
(spec/def ::params
  (spec/merge ::--params
              (spec/keys :req-un [::value])))

(spec/def ::args (spec/cat :params ::params))

(defn textfield
  [{:keys [id]
    :or   {id (util/gen-id)}}]
  (fn [& args]
    (let [{:keys [params]}            (util/conform! ::args args)
          {:keys [style
                  placeholder
                  label
                  value]
           :or   {style       {}
                  placeholder ""}} params
          ui-params                   (select-keys params (util/keys-from-spec ::--params))
          class                       (str/join " " [(util/params->classes ui-params)
                                                     (when (not (empty? value)) "dirty")])]
      [:div.Textfield {:key   (util/slug "textfield" id)
                       :style style
                       :class class}
       [:input (merge
                (dissoc ui-params :placeholder :label)
                (when (some? value) {:value value})
                {:type          :text
                 :placeholder   placeholder
                 :auto-complete "off"})]
       (when-not (empty? label)
         [:label {:for id} label])])))
