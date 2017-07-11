(ns ui.element.icon
  (:require #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [clojure.string :as str]
            [clojure.string :as str]
            [ui.util :as u]))


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
   :params ::icon-params
   :icon ::icon-name))


(defn icon
  [& args]
  (let [{:keys [params icon]}       (spec/conform ::icon-args args)
        {:keys [font size] :or {size 2}} params
        style                            {:font-size (str size "rem")}
        params                           (dissoc params :size :font)
        font                             (apply hash-map font)]
    (if-let [font-name (:font-name font)]
      [:i.Icon (merge {:class font-name :style style} params) icon]
      (let [font-prefix (:font-prefix font)
            class       (str/join " " [font-prefix (str font-prefix "-" icon)])]
        [:i.Icon (merge {:class class :style style} params)]))))


(spec/fdef icon
           :args ::icon-args
           :ret vector?)
