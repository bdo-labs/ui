(ns ui.docs.inputs
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as u]))


;; Subscriptions
(re-frame/reg-sub ::bacon? u/extract-or-false)
(re-frame/reg-sub ::cheese? u/extract-or-false)
(re-frame/reg-sub ::ketchup? u/extract-or-false)
(re-frame/reg-sub ::email? u/extract-or-false)
(re-frame/reg-sub ::collection-opened u/extract)
(re-frame/reg-sub ::multiple? u/extract-or-false)
(re-frame/reg-sub ::disabled? u/extract-or-false)

(re-frame/reg-sub ::query u/extract)
(re-frame/reg-sub ::collection u/extract)
(re-frame/reg-sub
 ::filtered-collection
 :<- [::collection]
 :<- [::query]
 (fn [[coll query] _]
   (if-not (empty? query)
     (filter (fn [item] (str/index-of item query)) coll)
     coll)))


;; Events
(re-frame/reg-event-db ::toggle-bacon? u/toggle)
(re-frame/reg-event-db ::toggle-cheese? u/toggle)
(re-frame/reg-event-db ::toggle-ketchup? u/toggle)
(re-frame/reg-event-db ::toggle-email? u/toggle)
(re-frame/reg-event-db ::toggle-multiple? u/toggle)
(re-frame/reg-event-db ::toggle-disabled? u/toggle)


(defn check-toggle []
  (let [bacon?         @(re-frame/subscribe [::bacon?])
        cheese?        @(re-frame/subscribe [::cheese?])
        ketchup?       @(re-frame/subscribe [::ketchup?])
        email?         @(re-frame/subscribe [::email?])
        toggle-bacon   #(re-frame/dispatch [::toggle-bacon?])
        toggle-cheese  #(re-frame/dispatch [::toggle-cheese?])
        toggle-ketchup #(re-frame/dispatch [::toggle-ketchup?])
        toggle-email   #(re-frame/dispatch [::toggle-email?])]
    [layout/horizontally
     [layout/vertically
      [element/checkbox {:checked?  bacon?
                         :on-change toggle-bacon} "Bacon"]
      [element/checkbox {:checked?  cheese?
                         :on-change toggle-cheese} "Cheese"]
      [element/checkbox {:checked?  ketchup?
                         :on-change toggle-ketchup} "Ketchup"]]
     [layout/vertically
      [element/toggle {:checked?  email?
                       :on-change toggle-email} "Eat here?"]]]))


(defn completion
  []
  (let [multiple?           @(re-frame/subscribe [::multiple?])
        disabled?           @(re-frame/subscribe [::disabled?])
        filtered-collection @(re-frame/subscribe [::filtered-collection])]
    [layout/horizontally
     [layout/vertically
      [element/checkbox {:checked?  multiple?
                         :on-change #(re-frame/dispatch [::toggle-multiple?])} "Multiple?"]
      [element/checkbox {:checked?  disabled?
                         :on-change #(re-frame/dispatch [::toggle-disabled?])} "Disabled?"]
      [element/auto-complete {:placeholder "Randomly generated strings"
                              :items       filtered-collection
                              :multiple?   multiple?
                              :disabled?   disabled?}]]]))


(defn documentation
  []
  [element/article
   "# Checkbox & Toggle
   Use checkboxes whenever there are multiple choices that are
   combined, whereas toggles are for switching on/off or between two
   choices.
   "
   [check-toggle]
   "# Auto-complete"
   [completion]])
