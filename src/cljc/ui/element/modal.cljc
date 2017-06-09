(ns ui.element.modal
  (:require [ui.element.containers :refer [container]]
            [ui.element.button :refer [button]]
            #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [ui.util :as u]))


(spec/def ::open? boolean?)
(spec/def ::confirm fn?)
(spec/def ::cancel-on-backdrop? boolean?)
(spec/def ::confirm-text
  (spec/and string? not-empty))
(spec/def ::cancel-text
  (spec/and string? not-empty))
(spec/def ::dialog-params
  (spec/keys :opt-un [::open?
                   ::confirm
                   ::cancel-on-backdrop?
                   ::confirm-text
                   ::cancel-text]))
(spec/def ::dialog-body
  (spec/or :string string?
        :formatted vector?))


(defn dialog
  [{:keys [confirm confirm-text cancel-text open cancel-on-backdrop]
    :or   {confirm      false
           confirm-text "Yes"
           cancel-text  "No"
           open         false
           cancel-on-backdrop true}
    :as   params} content]
  (let [classes (u/names->str (concat [(when (true? open) :Open)]
                                    (:class params)))]
    [:div.Dialog {:class classes}
     [:div.Backdrop {:on-click #(confirm false)}]
     [container {:direction "column"
                 :class     [:Dialog-content]}
      (into [:p] content)
      (when (fn? confirm)
        [container {:no-gap true :fill true :justify "center"}
         [button {:flat?     true
                  :on-click #(confirm true)} confirm-text]
         [button {:flat?     true
                  :on-click #(confirm false)} cancel-text]])]]))

(spec/fdef dialog
        :args (spec/cat :params ::dialog-params
                     :body ::dialog-body)
        :ret vector?
        :fn dialog)
