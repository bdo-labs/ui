(ns ui.element.icon.spec
  (:require [clojure.spec.alpha :as spec]))

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

(spec/def ::size pos?)

(spec/def ::icon-params
  (spec/keys
   :opt-un [::font ::size]))

(spec/def ::icon-args
  (spec/cat
   :params (spec/? ::icon-params)
   :icon ::icon-name))

(spec/fdef icon
           :args ::icon-args
           :ret vector?)
