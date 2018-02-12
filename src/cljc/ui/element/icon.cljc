(ns ui.element.icon
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [clojure.string :as str]
            [ui.util :as util]
            [re-frame.core :as re-frame]))


(spec/def ::font-prefix
  (spec/with-gen (spec/and string? #(> (count %) 1) #(< (count %) 4))
    #(spec/gen #{"ion"})))


(spec/def ::font-name
  (spec/with-gen (spec/and string? #(>= (count %) 4))
    #(spec/gen #{"material-icons"})))


(spec/def ::font
  (spec/or :font-name ::font-name
           :font-prefix ::font-prefix))


;; This will generate icon-names that are available in both ionicons &
;; material-icons
(spec/def ::icon-name
  (spec/with-gen (spec/and string? not-empty)
    #(spec/gen #{"email" "help" "home" "loop" "pause"
                 "search" "settings" "shuffle" "stop"})))


(spec/def ::size nat-int?)


(spec/def ::icon-params
  (spec/keys
   :opt-un [::font ::size]))


(spec/def ::icon-args
  (spec/cat
   :params (spec/? ::icon-params)
   :icon ::icon-name))


(re-frame/reg-event-fx
 :init-icons
 (fn [{:keys [db]}]
   {:dispatch [:ui/icon-font "ion"]
    :db db}))


(re-frame/reg-event-db
 :ui/icon-font
 (fn [db [k font]]
   (let [font (apply hash-map (util/conform! ::font font))]
     (assoc db k font))))


(re-frame/reg-sub :ui/icon-font util/extract)


(defn icon
  [& args]
  (let [{:keys [params icon]} (util/conform! ::icon-args args)
        {:keys [font size]
         :or   {font @(re-frame/subscribe [:ui/icon-font])
                size 2}}      params
        style                 {:font-size (str size "rem")}
        class                 (:class params)
        params                (dissoc params :size :font :class)
        font                  (if (vector? font) (apply hash-map font) font)]
    (if-let [font-name (:font-name font)]
      [:i.Icon (merge {:class (str font-name " " class) :style style} params) icon]
      (let [font-prefix (:font-prefix font)
            class       (str/join " " (conj [font-prefix (str font-prefix "-" icon)] class))]
        [:i.Icon (merge {:class class :style style} params)]))))


(spec/fdef icon
           :args ::icon-args
           :ret vector?)
