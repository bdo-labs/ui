(ns ui.element.textfield
  (:require [clojure.spec.alpha :as spec]
            [ui.util :as u]))


(spec/def ::placeholder (spec/and string? not-empty))
(spec/def ::value string?)
(spec/def ::disabled? boolean?)
(spec/def ::read-only? boolean?)
(spec/def ::focus? boolean?)


(spec/def ::textfield-params
  (spec/keys :opt-un [::value ::placeholder ::disabled? ::read-only? ::focus?]))


(spec/def ::textfield-args (spec/cat :params ::textfield-params))


(defn textfield
  [{:keys [on-change on-focus on-key-down on-blur]}]
  (let [!parent-el (atom nil)
        id         (u/gen-id)]
    (fn [& args]
      (let [{:keys [params]}                (u/conform-or-fail ::textfield-args args)
            {:keys [value placeholder class disabled? read-only? ghost focus?]
             :or   {disabled?  false
                    read-only? false
                    ghost      ""}} params
            id (or (:id params) id)]
        (when-let [parent-el @!parent-el]
          (when focus?
            (.focus (.-firstChild parent-el))))
        (let [classes (u/names->str [(when disabled? :disabled)
                                     (when read-only? :read-only)
                                     (when (not-empty value) :dirty)
                                     class])]
          [:div.Textfield {:class classes
                           :ref   #(reset! !parent-el %)}
           [:input (merge
                    (dissoc params :class :placeholder :read-only? :disabled? :focus?)
                    {:id           id
                     :type         :text
                     :autoComplete "off"
                     :read-only    read-only?
                     :disabled     disabled?
                     :placeholder  ""})]
           [:label {:for id} placeholder]])))))


(spec/fdef textfield
        :args ::textfield-args
        :ret vector?)
