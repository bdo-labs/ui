(ns ui.element.textfield
  (:require [clojure.spec :as spec]
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
(spec/def ::placeholder string?)
(spec/def ::value string?)
(spec/def ::disabled boolean?)
(spec/def ::read-only boolean?)
(spec/def ::focus boolean?)
(spec/def ::params
  (spec/keys :req-un [::id]
             :opt-un [::value
                      ::placeholder
                      ::disabled
                      ::read-only
                      ::focus
                      ::on-change
                      ::on-focus
                      ::on-blur
                      ::on-key-up
                      ::on-key-down]))


(spec/def ::args (spec/cat :params ::params))


(defn textfield
  [{:keys [id]}]
  (fn [& args]
    (let [{:keys [params]}   (util/conform-or-fail ::args args)
          {:keys [style placeholder focus]
           :or   {style {}}} params
          ui-params          (util/keys-from-spec ::params)
          class              (util/params->classes params)]
      [:div.Textfield {:key   (str "textfield-" id)
                       :style style
                       :class class}
       [:input (merge
                (dissoc params :class :style :placeholder)
                {:type          :text
                 :auto-complete "off"})]
       (when-not (empty? placeholder)
         [:label {:for id} placeholder])])))


(spec/fdef textfield
           :args ::args
           :ret vector?)
