(ns ui.element.notification
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [ui.util :as util]))


;; Parameters
(spec/def ::id (spec/and string? #(re-find #"(?i)(\w+)" %)))
(spec/def ::value string?)
(spec/def ::model util/deref?)
(spec/def ::class (spec/or :string string? :coll-of-strings (spec/coll-of string?)))
(spec/def ::params
  (spec/keys :opt-un [::id
                      ::value
                      ::class
                      ::model]))


(spec/def ::args (spec/cat :params ::params))


(defn notification
  [& args]
  (let [{:keys [params]}             (util/conform! ::args args)
        {:keys [id model]
         :or   {id (util/gen-id)}}   params]
    (fn [& args]
      (let [{:keys [params]}         (util/conform! ::args args)
            {:keys [style
                    class
                    value]
             :or   {style    {}
                    value    ""
                    class    []}}    params
            ui-params                (select-keys params (util/keys-from-spec ::params))
            class                    (str/join " " (flatten [(util/params->classes ui-params)
                                                             [class]]))]
        (let [-value (if model (or @model value) (or value ""))
              extra-style(if (= -value ::hide)
                           {:display "none;"}
                           {})]
          [:div.Notification {:key (util/slug "notification" id)
                              :style (merge style extra-style)
                              :class class}
           -value])))))

(spec/def ::notification ::params)
(spec/def ::notifications-params (spec/keys :opt-un [::id
                                                     ::class
                                                     ::notification]
                                            :req-un [::model]))
(spec/def ::notifications-args (spec/cat :params ::notifications-params))

(defn notifications
  [& args]
  (let [{:keys [params]}             (util/conform! ::notifications-args args)
        {:keys [id model]
         :or   {id (util/gen-id)}}   params]
    (fn [& args]
      (let [{:keys [params]}         (util/conform! ::notifications-args args)
            notification-params (:notification params)
            {:keys [style
                    class
                    value]
             :or   {style    {}
                    value    ""
                    class    []}}    params
            ui-params                (select-keys params (util/keys-from-spec ::notifications-params))
            class                    (str/join " " (flatten [(util/params->classes ui-params)
                                                             [class]]))]
        (let [values @model]
          [:div.Notifications {:key (util/slug "notifications" id)
                               :style style
                               :class class}
           (for [value values]
             ^{:key (util/slug "notification" id "-sub-" (str value))}
             [notification (assoc notification-params
                                  :value value)])])))))
