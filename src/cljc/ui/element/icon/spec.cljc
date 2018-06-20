(ns ui.element.icon.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]))

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
(spec/def ::name
  (spec/with-gen (spec/and string? #(not-empty %))
    #(spec/gen #{"add"
                 "call"
                 "camera"
                 "close"
                 "cloud"
                 "email"
                 "folder"
                 "help"
                 "home"
                 "image"
                 "loop"
                 "map"
                 "notifications"
                 "pause"
                 "person"
                 "search"
                 "settings"
                 "shuffle"
                 "stop"})))

(spec/def ::size
  (spec/with-gen pos?
    #(gen/return 10)))

(spec/def ::params
  (spec/keys
   :opt-un [::font ::size]))

(spec/def ::args
  (spec/cat
   :params (spec/? ::params)
   :content ::name))

(spec/fdef icon
           :args ::args
           :ret vector?)
