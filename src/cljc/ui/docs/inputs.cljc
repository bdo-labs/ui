(ns ui.docs.inputs
  (:require [clojure.string :as str]
            [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as util]
            [clojure.spec.alpha :as spec]))


(def characters
  #{"Alliser Thorne"
   "Arya Stark"
   "Barristan Selmy"
   "Beric Dondarrion"
   "Bran Stark"
   "Brienne of Tarth"
   "Bronn"
   "Catelyn Stark"
   "Cersei Lannister"
   "Daario Naharis"
   "Daenerys Targaryen"
   "Davos Seaworth"
   "Doreah"
   "Eddard (Ned) Stark"
   "Eddison Tollett"
   "Ellaria Sand"
   "Gendry"
   "Gilly"
   "Grand Maester Pycelle"
   "Gregor Clegane"
   "Grenn"
   "Grey Worm"
   "Hodor"
   "Hot Pie"
   "Irri"
   "Jaime Lannister"
   "Janos Slynt"
   "Jaqen H'ghar"
   "Jeor Mormont"
   "Joffrey Baratheon"
   "Jojen Reed"
   "Jon Snow"
   "Jorah Mormont"
   "Kevan Lannister"
   "Khal Drogo"
   "Lancel Lannister"
   "Little Sam"
   "Loras Tyrell"
   "Mace Tyrell"
   "Maester Aemon"
   "Maester Luwin"
   "Margaery Tyrell"
   "Meera Reed"
   "Melisandre"
   "Meryn Trant"
   "Missandei"
   "Myrcella Baratheon"
   "Olenna Tyrell"
   "Olly"
   "Osha"
   "Othell Yarwyck"
   "Petyr (Littlefinger) Baelish"
   "Podrick Payne"
   "Pypar"
   "Qyburn"
   "Ramsay Bolton"
   "Rast"
   "Rickon Stark"
   "Robb Stark"
   "Robert Baratheon"
   "Rodrik Cassel"
   "Roose Bolton"
   "Ros"
   "Samwell Tarly"
   "Sandor (The Hound) Clegane"
   "Sansa Stark"
   "Selyse Florent"
   "Shae"
   "Shireen Baratheon"
   "Stannis Baratheon"
   "Talisa Maegyr"
   "The Waif"
   "Theon Greyjoy"
   "Thoros of Myr"
   "Tommen Baratheon"
   "Tormund Giantsbane"
   "Tyrion Lannister"
   "Tywin Lannister"
   "Varys"
   "Viserys Targaryen"
   "Yara Greyjoy"
   "Ygritte"})


(re-frame/reg-event-db
 :init-inputs
 (fn [db _]
   (let [assoc-id (fn [n character] {:id n :value character})
         items    (->> characters
                       (map-indexed assoc-id))]
     (assoc db ::items (set (sort-by :value items))))))


;; Subscriptions
(re-frame/reg-sub ::bacon util/extract-or-false)
(re-frame/reg-sub ::cheese util/extract-or-false)
(re-frame/reg-sub ::ketchup util/extract-or-false)
(re-frame/reg-sub ::email util/extract-or-false)
(re-frame/reg-sub ::items-opened util/extract)
(re-frame/reg-sub ::multiple util/extract-or-false)
(re-frame/reg-sub ::disabled util/extract-or-false)
(re-frame/reg-sub ::query util/extract)
(re-frame/reg-sub ::items util/extract)


(re-frame/reg-sub
 ::filtered-items
 :<- [::items]
 :<- [::query]
 (fn [[coll query] _]
   (let [items (if-not (empty? query)
                 (filter (fn [item] (str/index-of item query)) coll)
                 coll)]
     items)))


(re-frame/reg-sub
 ::sep-filtered-items
 :<- [::items]
 :<- [::query]
 (fn [[coll query] _]
   (let [items (if-not (empty? query)
                  (filter (fn [item] (str/index-of item query)) coll)
                  coll)]
     items)))


;; Events
(re-frame/reg-event-db ::toggle-bacon util/toggle)
(re-frame/reg-event-db ::toggle-cheese util/toggle)
(re-frame/reg-event-db ::toggle-ketchup util/toggle)
(re-frame/reg-event-db ::toggle-email util/toggle)
(re-frame/reg-event-db ::toggle-multiple util/toggle)
(re-frame/reg-event-db ::toggle-disabled util/toggle)


(defn check-toggle []
  (let [bacon          @(re-frame/subscribe [::bacon])
        cheese         @(re-frame/subscribe [::cheese])
        ketchup        @(re-frame/subscribe [::ketchup])
        email          @(re-frame/subscribe [::email])
        toggle-bacon   #(re-frame/dispatch [::toggle-bacon])
        toggle-cheese  #(re-frame/dispatch [::toggle-cheese])
        toggle-ketchup #(re-frame/dispatch [::toggle-ketchup])
        toggle-email   #(re-frame/dispatch [::toggle-email])]
    [layout/horizontally
     [layout/vertically
      [element/checkbox {:checked   bacon
                         :on-change toggle-bacon} "Bacon"]
      [element/checkbox {:checked   cheese
                         :on-change toggle-cheese} "Cheese"]
      [element/checkbox {:checked   ketchup
                         :on-change toggle-ketchup} "Ketchup"]]
     [layout/vertically
      [element/toggle {:checked   email
                       :on-change toggle-email} "Eat here?"]]]))


(defn smart-case-includes? [s substr]
  (if-not (empty? (re-find #"[A-Z]" substr))
    (str/includes? s substr)
    (str/includes? (str/lower-case s) (str/lower-case substr))))


(def selected* (atom #{}))


(defn completion
  []
  (let [multiple           @(re-frame/subscribe [::multiple])
        disabled           @(re-frame/subscribe [::disabled])
        filtered-items     @(re-frame/subscribe [::filtered-items])
        sep-filtered-items @(re-frame/subscribe [::sep-filtered-items])]
    [layout/horizontally
     [layout/vertically
      [element/checkbox {:checked   multiple
                         :on-change #(re-frame/dispatch [::toggle-multiple])} "Multiple"]
      [element/checkbox {:checked   disabled
                         :on-change #(re-frame/dispatch [::toggle-disabled])} "Disabled"]
      [element/chooser {:style           {:width "420px"}
                        :label           "Name a Character from Game of Thrones"
                        :searchable      true
                        :add-message     "Add % to members"
                        :empty-message   "No results matching %"
                        :max-items       1
                        :on-select       #(do
                                           (util/log %)
                                           (reset! selected* %))
                        :items           (set sep-filtered-items)
                        :predicate?      smart-case-includes?
                        :close-on-select false
                        :multiple        multiple
                        :disabled        disabled
                        :labels          false}]]]))



(defn documentation
  []
  [element/article
   "### Checkbox & Toggle
   Use checkboxes whenever there are multiple choices that are
   combined, whereas toggles are for switching on/off or between two
   choices.
   "
   [check-toggle]
   [completion]])

