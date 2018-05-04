(ns ui.docs.card
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [#?(:clj clj-time.core :cljs cljs-time.core) :as time]
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
  (let [messages  #{{:id    1
                     :label (label "Do not take life too seriously" "You will never get out of it alive")
                     :value "alive"}
                    {:id    2
                     :label (label "A day without sunshine is like," "you know, night")
                     :value "night"}}
        on-click  #(re-frame/dispatch [::toggle-show])
        selected* (atom nil)]
    (fn []
      [layout/vertically {:fill?      true
                          :background :white
                          :style      {:padding       "4em"
                                       :margin-bottom "4em"}}
       [:div {:style {:margin "2em" :width "600px"}}
        [element/card {:rounded? true :gap? false :fill? true}
         [element/collection {:collapsable true
                              :sorted      false} messages]]
        [element/months {:every 3
                         :max (time/now)
                         :model selected*}]
        [element/days {:on-click #(util/log "foo")
                       :max (time/now)
                       :nav? false}]]])))
