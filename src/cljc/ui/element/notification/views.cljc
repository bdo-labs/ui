(ns ui.element.notification.views
  (:require [clojure.string :as str]
            [ui.element.notification.spec :as spec]
            [ui.util :as util]))


(defn notification
  [& args]
  (let [{:keys [params]}             (util/conform! ::spec/args args)
        {:keys [id model]
         :or   {id (util/gen-id)}}   params]
    (fn [& args]
      (let [{:keys [params]}         (util/conform! ::spec/args args)
            {:keys [style
                    class
                    value]
             :or   {style    {}
                    value    ""
                    class    []}}    params
            ui-params                (select-keys params (util/keys-from-spec ::spec/params))
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


(defn notifications
  [& args]
  (let [{:keys [params]}             (util/conform! ::spec/notifications-args args)
        {:keys [id model]
         :or   {id (util/gen-id)}}   params]
    (fn [& args]
      (let [{:keys [params]}         (util/conform! ::spec/notifications-args args)
            notification-params (:notification params)
            {:keys [style
                    class
                    value]
             :or   {style    {}
                    value    ""
                    class    []}}    params
            ui-params                (select-keys params (util/keys-from-spec ::spec/notifications-params))
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
