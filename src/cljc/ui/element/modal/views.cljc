(ns ui.element.modal.views
  (:require [ui.element.containers.views :refer [container]]
            [ui.element.modal.spec :as spec]
            [ui.element.button.views :refer [button]]
            [ui.element.icon.views :refer [icon]]
            [ui.util :as util]))

(defn dialog [& args]
  (let [{:keys [params content]}        (util/conform! ::spec/args args)
        {:keys [show? backdrop? cancel-on-backdrop? close-button? on-cancel]
         :or   {backdrop?           true
                cancel-on-backdrop? true
                close-button?       true}} params
        class                           (util/params->classes params)
        ui-params                       (conj (util/keys-from-spec ::spec/params) :class)
        params                          (->> (apply dissoc params ui-params)
                                             (merge {:class class}))]
    (when show?
      [:div.Dialog params
       [:div.Content
        (into [container {:raised?  true
                          :rounded? true
                          :inline?  true
                          :gap?     false
                          :layout   :vertically
                          :style    {:background :white}}
               (when close-button?
                 [icon {:font     "ion"
                        :size     3
                        :class    "Close"
                        :on-click on-cancel} "ios-close-empty"])]
              (mapv last content))]
       (when backdrop?
         [:div.Backdrop (when cancel-on-backdrop? {:on-click on-cancel})])])))

(defn confirm-dialog [& args]
  (let [{:keys [params content]} (util/conform! ::spec/confirm-args args)
        {:keys [on-confirm on-cancel cancel-label confirm-label]
         :or {cancel-label "No"
              confirm-label "Yes"}} params
        ui-params (util/keys-from-spec ::spec/confirm-params)
        params (assoc (apply dissoc params ui-params) :on-cancel on-cancel)]
    [dialog params
     [container {:layout :vertically}
      [:p (map last content)]
      [container {:layout :horizontally}
       [button {:on-click on-confirm} confirm-label]
       [button {:on-click on-cancel} cancel-label]]]]))

(defn alert-dialog [& args]
  (let [{:keys [params content]} (util/conform! ::spec/alert-args args)
        {:keys [alert-label on-cancel]
         :or {alert-label "OK"}} params]
    [dialog params
     [:h3 (map last content)]
     [container {:layout :centered}
      [button {:on-click on-cancel} alert-label]]]))
