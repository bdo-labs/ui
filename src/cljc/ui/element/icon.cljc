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


;; This will generate icon-names that are available in both ionicons &
;; material-icons
(spec/def ::icon-name
  (spec/with-gen (spec/and string? not-empty)
    #(spec/gen #{"email" "help" "home" "loop" "pause"
                 "search" "settings" "shuffle" "stop"})))


(spec/def ::size #{"small" "medium" "large"})


(spec/def ::icon-params
  (spec/keys
   :req-un [(or ::font-name ::font-prefix)]
   :opt-un [::size]))


(spec/def ::icon-args
  (spec/cat
   :params ::icon-params
   :icon ::icon-name))


(defn icon
  [params icon-name]
  (let [
        args  (spec/conform ::icon-args [params icon-name])
        style (case (:size params)
                "small" {:font-size "1em"}
                "medium" {:font-size "2em"}
                "large"  {:font-size "4em"}
                {})]
    (if-let [font (:font-name params)]
      [:i (merge (dissoc params
                         :size
                         :font-prefix
                         :font-name) {:class font :style style}) icon-name]
      (let [prefix  (:font-prefix params)
            classes (str/join " " [prefix (str prefix "-" icon-name)])]
        [:i (merge (dissoc params :size) {:class classes
                                          :style style})]))))


(spec/fdef icon
           :args ::icon-args
           :ret vector?)
