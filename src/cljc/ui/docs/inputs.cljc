(ns ui.docs.inputs
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as util]
            [clojure.spec :as spec]))


(def characters
  #{"Eddard (Ned) Stark"
   "Robert Baratheon"
   "Jaime Lannister"
   "Catelyn Stark"
   "Cersei Lannister"
   "Daenerys Targaryen"
   "Jorah Mormont"
   "Petyr (Littlefinger) Baelish"
   "Viserys Targaryen"
   "Jon Snow"
   "Sansa Stark"
   "Arya Stark"
   "Robb Stark"
   "Theon Greyjoy"
   "Bran Stark"
   "Joffrey Baratheon"
   "Sandor (The Hound) Clegane"
   "Tyrion Lannister"
   "Khal Drogo"
   "Tywin Lannister"
   "Davos Seaworth"
   "Samwell Tarly"
   "Margaery Tyrell"
   "Stannis Baratheon"
   "Melisandre"
   "Jeor Mormont"
   "Bronn"
   "Varys"
   "Shae"
   "Ygritte"
   "Talisa Maegyr"
   "Gendry"
   "Tormund Giantsbane"
   "Gilly"
   "Brienne of Tarth"
   "Ramsay Bolton"
   "Ellaria Sand"
   "Daario Naharis"
   "Missandei"
   "Jaqen H'ghar"
   "Tommen Baratheon"
   "Roose Bolton"
   "Grand Maester Pycelle"
   "Meryn Trant"
   "Hodor"
   "Grenn"
   "Osha"
   "Rickon Stark"
   "Ros"
   "Gregor Clegane"
   "Janos Slynt"
   "Lancel Lannister"
   "Myrcella Baratheon"
   "Rodrik Cassel"
   "Maester Luwin"
   "Irri"
   "Doreah"
   "Kevan Lannister"
   "Barristan Selmy"
   "Rast"
   "Maester Aemon"
   "Pypar"
   "Alliser Thorne"
   "Othell Yarwyck"
   "Loras Tyrell"
   "Hot Pie"
   "Beric Dondarrion"
   "Podrick Payne"
   "Eddison Tollett"
   "Yara Greyjoy"
   "Selyse Florent"
   "Little Sam"
   "Grey Worm"
   "Qyburn"
   "Olenna Tyrell"
   "Shireen Baratheon"
   "Meera Reed"
   "Jojen Reed"
   "Thoros of Myr"
   "Olly"
   "Mace Tyrell"
   "The Waif"})


(re-frame/reg-event-db
 :init-inputs
 (fn [db _]
   (let [assoc-id (fn [n character] {:id n :value character})
         items    (->> characters
                       (map-indexed assoc-id))]
     (assoc db ::items items))))


;; Subscriptions
(re-frame/reg-sub ::bacon? util/extract-or-false)
(re-frame/reg-sub ::cheese? util/extract-or-false)
(re-frame/reg-sub ::ketchup? util/extract-or-false)
(re-frame/reg-sub ::email? util/extract-or-false)
(re-frame/reg-sub ::items-opened util/extract)
(re-frame/reg-sub ::multiple? util/extract-or-false)
(re-frame/reg-sub ::disabled? util/extract-or-false)
(re-frame/reg-sub ::query util/extract)
(re-frame/reg-sub ::items util/extract)


(re-frame/reg-sub
 ::filtered-items
 :<- [::items]
 :<- [::query]
 (fn [[coll query] _]
   (if-not (empty? query)
     (filter (fn [item] (str/index-of item query)) coll)
     coll)))


;; Events
(re-frame/reg-event-db ::toggle-bacon? util/toggle)
(re-frame/reg-event-db ::toggle-cheese? util/toggle)
(re-frame/reg-event-db ::toggle-ketchup? util/toggle)
(re-frame/reg-event-db ::toggle-email? util/toggle)
(re-frame/reg-event-db ::toggle-multiple? util/toggle)
(re-frame/reg-event-db ::toggle-disabled? util/toggle)


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
      [element/checkbox {:checked   bacon?
                         :on-change toggle-bacon} "Bacon"]
      [element/checkbox {:checked   cheese?
                         :on-change toggle-cheese} "Cheese"]
      [element/checkbox {:checked   ketchup?
                         :on-change toggle-ketchup} "Ketchup"]]
     [layout/vertically
      [element/toggle {:checked   email?
                       :on-change toggle-email} "Eat here?"]]]))


(defn smart-case-includes? [s substr]
  (if-not (empty? (re-find #"[A-Z]" substr))
    (str/includes? s substr)
    (str/includes? (str/lower-case s) (str/lower-case substr))))


(defn completion
  []
  (let [multiple?      @(re-frame/subscribe [::multiple?])
        disabled?      @(re-frame/subscribe [::disabled?])
        filtered-items @(re-frame/subscribe [::filtered-items])]
    [layout/horizontally
     [layout/vertically
      [element/checkbox {:checked   multiple?
                         :on-change #(re-frame/dispatch [::toggle-multiple?])} "Multiple?"]
      [element/checkbox {:checked   disabled?
                         :on-change #(re-frame/dispatch [::toggle-disabled?])} "Disabled?"]
      [element/chooser {:id          "game-of-thrones"
                        :placeholder "Name a Character from Game of Thrones"
                        :searchable  true
                        :items       filtered-items
                        :predicate? smart-case-includes?
                        :multiple   multiple?
                        :disabled   disabled?}]]]))


(defn documentation
  []
  [element/article
   "### Checkbox & Toggle
   Use checkboxes whenever there are multiple choices that are
   combined, whereas toggles are for switching on/off or between two
   choices.
   "
   [check-toggle]
   "### Chooser"
   [completion]])

