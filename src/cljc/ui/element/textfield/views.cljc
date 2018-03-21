(ns ui.element.textfield.views
  (:require [ui.util :as util]
            [ui.element.textfield.spec :as spec]
            [clojure.string :as str]))

(defn textfield
  [{:keys [id]
    :or   {id (util/gen-id)}}]
  (fn [& args]
    (let [{:keys [params]}            (util/conform! ::spec/args args)
          {:keys [style
                  placeholder
                  label
                  value]
           :or   {style       {}
                  placeholder ""}} params
          ui-params                   (select-keys params (util/keys-from-spec ::spec/--params))
          class                       (str/join " " [(util/params->classes ui-params)
                                                     (when (or (not (empty? value))
                                                               (not (empty? placeholder))) "dirty")])]
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
