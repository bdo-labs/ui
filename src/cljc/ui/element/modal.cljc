(ns ui.element.modal
  (:require [ui.element.containers :refer [container]]
            [ui.element.button :refer [button]]
            [clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [ui.util :as u]))


#_(spec/def ::open? boolean?)
#_(spec/def ::confirm fn?)
#_(spec/def ::cancel-on-backdrop? boolean?)
#_(spec/def ::confirm-text (spec/and string? not-empty))
#_(spec/def ::cancel-text (spec/and string? not-empty))
#_(spec/def ::dialog-params
  (spec/keys :opt-un [::open?
                   ::confirm
                   ::cancel-on-backdrop?
                   ::confirm-text
                   ::cancel-text]))
#_(spec/def ::dialog-body
  (spec/or :string string?
        :formatted vector?))


(spec/def ::show? boolean?)
(spec/def ::cancel-on-backdrop? boolean?)


(spec/def ::params
  (spec/keys :opt-un [::show?
                      ::cancel-on-backdrop?]))


(spec/def ::content (spec/* (spec/or :str string? :vec vector?)))


(spec/def ::args
  (spec/cat :params ::params
            :content ::content))


(defn dialog [& args]
  (let [{:keys [params content]} (u/conform-or-fail ::args args)
        class (u/params->classes params)
        ui-params (conj (u/keys-from-spec ::params) :class)
        params (->> (apply dissoc params ui-params)
                    (merge {:class class}))]
    [:div.Dialog params
     [:pre (pr-str params)]
     [:div.Backdrop {}
      [container {:raised? true
                  :rounded? true
                  :background "white"
                  :layout :vertically}
       (map last content)]]]))


(spec/def ::stub
  (spec/with-gen fn?
    (gen/return (constantly nil))))


(spec/def ::confirm-label string?)
(spec/def ::cancel-label string?)
(spec/def ::on-confirm ::stub)
(spec/def ::on-cancel ::stub)


(spec/def ::confirm-params
  (spec/keys :opt-un [::confirm-label
                      ::cancel-label
                      ::on-cancel]
             :req-un [::on-confirm]))


(spec/def ::confirm-args
  (spec/cat :params ::confirm-params
            :content ::content))


(defn confirm-dialog [& args]
  (let [{:keys [params content]} (u/conform-or-fail ::confirm-args args)
        {:keys [on-cancel on-confirm cancel-label confirm-label]
         :or {cancel-label "No"
              confirm-label "Yes"
              on-cancel #(u/log "Cancel")}} params
        ui-params (u/keys-from-spec ::confirm-params)
        params (apply dissoc params ui-params)]
    [dialog params
     [:h3 "voi voi"]
     #_[container {:layout :vertically}
      [:pre (pr-str content)]
      [container {:layout :horizontally}
       [button {:on-click on-cancel} cancel-label]
       [button {:on-click on-confirm} confirm-label]]]]))



#_(defn dialog
  [{:keys [confirm confirm-text cancel-text open cancel-on-backdrop]
    :or   {confirm            false
           confirm-text       "Yes"
           cancel-text        "No"
           open               false
           cancel-on-backdrop true}
    :as   params} content]
  (let [classes (u/names->str (concat [(when (true? open) :Open)]
                                      (:class params)))]
    [:div.Dialog {:class classes}
     [:div.Backdrop {:on-click #(confirm false)}]
     #_[container {:raised? true
                 :background "white"
                 :rounded? true
                 :layout :vertically}
      [:h3 "Dialog"]]]))

(spec/fdef dialog
        :args (spec/cat :params ::dialog-params
                     :body ::dialog-body)
        :ret vector?
        :fn dialog)
