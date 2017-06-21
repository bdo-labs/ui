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


(spec/def ::size #{"small" "medium" "large"})


(spec/def ::icon-params
  (spec/keys
   :opt-un [::font ::size]))


(spec/def ::icon-args
  (spec/cat
   :params ::icon-params
   :icon ::icon-name))


(defn icon
  [params icon-name]
  (let [args  (spec/conform ::icon-args [params icon-name])
        font  (apply hash-map (-> args :params :font))
        style (case (:size params)
                "small"  {:font-size "1em"}
                "medium" {:font-size "1.4em"}
                "large"  {:font-size "2.8em"}
                {})]
    (if-let [font-name (:font-name font)]
      [:i (merge (dissoc params
                         :size
                         :font) {:class font-name :style style}) icon-name]
      (let [font-prefix (:font-prefix font)
            classes     (str/join " " [font-prefix (str font-prefix "-" icon-name)])]
        [:i (merge (dissoc params
                           :size
                           :font) {:class classes
                                   :style style})]))))


(spec/fdef icon
           :args ::icon-args
           :ret vector?)
