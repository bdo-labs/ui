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
                    class]
             :or   {style    {}
                    class    []}}    params
            ui-params                (select-keys params (util/keys-from-spec ::spec/params))
            class                    (str/join " " (flatten [(util/params->classes ui-params)
                                                             [class]]))]
        (let [-value (if (util/deref? model) @model (or model ""))
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
        (let [values (if (util/deref? model) @model model)
              index-values (map vector (range (count values)) values)]
          [:div.Notifications {:key (util/slug "notifications" id)
                               :style style
                               :class class}
           (doall
            (for [[index value] index-values]
              (let [value (if (util/deref? value) @value value)]
                ;; BUG: react for some reason does not like to have the id + index as a
                ;; unique key and treats all notification elements with that
                ;; key format as the same. using id + value works, but bugs out when the
                ;; values are the same and of course is meaningless as a key since it
                ;; changes all the time. id + index + value is the current solution, but
                ;; suffers the same problem of changing all the time
                (let [notification-id (util/slug "notification" (str id index value) "-sub")]
                  ^{:key notification-id}
                  [notification (assoc notification-params :model value)]))))])))))
