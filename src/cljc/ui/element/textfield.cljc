(ns ui.element.textfield
  (:require [clojure.spec :as spec]
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
  (let [!parent-el (clojure.core/atom nil)
        id         (u/gen-id)]
    (fn [& args]
      (let [{:keys [params]}           (u/conform-or-fail ::textfield-args args)
            {:keys [value style placeholder class disabled? read-only? focus?]
             :or   {disabled?  false
                    style      {}
                    read-only? false}} params]
        (when-let [parent-el @!parent-el]
          (when focus?
            (.focus (.-firstChild parent-el))))
        (let [classes (u/names->str [(when disabled? :disabled)
                                     (when read-only? :read-only)
                                     (when (not-empty value) :dirty)
                                     (when-not (nil? placeholder) :placeholder)
                                     class])]
          [:div.Textfield {:class classes
                           :ref   #(reset! !parent-el %)
                           :style style}
           [:input (merge
                    (dissoc params :class :placeholder :read-only? :disabled? :focus? :style)
                    {:id            id
                     :type          :text
                     :auto-complete "off"
                     :read-only     read-only?
                     :disabled      disabled?
                     :placeholder   ""})]
           (when-not (nil? placeholder)
             [:label {:for id} placeholder])])))))


(spec/fdef textfield
        :args ::textfield-args
        :ret vector?)
