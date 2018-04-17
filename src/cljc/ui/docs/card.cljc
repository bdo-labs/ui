(ns ui.docs.card
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as util]
            [re-frame.core :as re-frame]
            [clojure.spec.alpha :as spec]))

(def db
  {::show? false})

(defn multiple? [s]
  (and (seq s) (> (count s) 1)))

(re-frame/reg-event-db
 ::toggle-show
 (fn [db _]
   (update-in db [::show?] not)))

(re-frame/reg-sub ::show? (fn [db path] (get-in db path)))

(defn label [title description]
  [layout/horizontally {:gap? false :aligned :middle}
   [element/icon {:font "material-icons"} "info"]
   [:h4 title]])

(defn documentation []
  (let [message {:id    0
                 :label (label "Issues with retrieving customer-data" "We are currently experiencing problems with Safebase's data")
                 :value (str (char (rand-int 100)))}
        messages (->> message
                      (repeat)
                      (take 10)
                      (map-indexed (fn [n m] (update-in m [:id] (partial + n))))
                      (set))
        on-click #(re-frame/dispatch [::toggle-show])]
    [layout/vertically {:fill? true
                        :background :white
                        :style {:padding "4em"
                                :margin-bottom "4em"}}
     [:div {:style {:margin "2em" :width "600px"}}
      [element/card {:rounded? true :gap? false :fill? true}
       [element/collection {:collapsable true} messages]]]]))
