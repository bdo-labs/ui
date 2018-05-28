(ns ui.docs.buttons
  (:require [ui.element.button.spec :as button]
            [ui.elements :as element]
            [ui.layout :as layout]
            [clojure.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            #?(:cljs [reagent.core :as reagent])
            [clojure.string :as str]
            [clojure.set :as set]
            [ui.spec-helper :as spec-helper]
            [ui.wire.polyglot :as polyglot]
            [ui.util :as util]))

(defn elem-path [elem]
  (let [path (keyword (subs (str elem) 2))]
    path))

;; Events -----------------------------------------------------------------

(re-frame/reg-event-db
 ::exercise-spec
 (fn [db [k elem]]
   (let [literal-elem  (subs (str elem) 2)
         specification (keyword (str/replace literal-elem #"views.+" "spec/args"))
         temp-gen      (last (last (spec/exercise specification 100)))
         params-form   (->> (spec/form (keyword (subs (str/replace (str specification) "args" "params") 1)))
                            (rest)
                            (apply hash-map))
         params        (->> params-form
                            (:opt-un)
                            (mapv (fn [param]
                                    {:spec     param
                                     :required false
                                     :describe (spec/describe param)
                                     :name     (name param)
                                     :value    (get-in temp-gen [:params (keyword (name param))] nil)})))
         gen           (assoc-in temp-gen [:params] params)]
     (assoc-in db [::state (elem-path elem)] gen))))

(re-frame/reg-event-db
 ::toggle-parameterlist
 (fn [db [k value]]
   (assoc-in db [::show-parameterlist?] value)))

(re-frame/reg-event-db
 ::toggle-param
 (fn [db [k elem param]]
   (update-in db [::state (elem-path elem) :params]
              (fn [params]
                (map #(when (= (:name %) param)
                        (update-in % [:value] not)) params)))))

;; Subscriptions ----------------------------------------------------------

(re-frame/reg-sub ::show-parameterlist? util/extract)

(re-frame/reg-sub
 ::state
 (fn [db [k elem]]
   (let [state (get-in db [k (elem-path elem)])]
    state)))

(re-frame/reg-sub
 ::params
 (fn [[k elem]]
   (re-frame/subscribe [::state elem]))
 (fn [state]
   (get-in state [:params] [])))

(re-frame/reg-sub
 ::params-map
 (fn [[k elem]]
   (re-frame/subscribe [::params elem]))
 (fn [params]
   (->> params
        (map #(select-keys % [:name :value]))
        (map vals)
        (flatten)
        (apply hash-map))))

(re-frame/reg-sub
 ::content
 (fn [[k elem]]
   (re-frame/subscribe [::state elem]))
 (fn [state]
   (get-in state [:content] nil)))

(defn preview [elem]
  (let [params  @(re-frame/subscribe [::params-map elem])
        content @(re-frame/subscribe [::content elem])]
    [layout/centered {:class "demo"
                      :width 2
                      :fill? true
                      :style {:position :relative
                              :height   "35rem"}}
     [:a {:href   "https://github.com/bdo-labs/ui/issues/new"
          :target :_blank}
      [element/icon {:title (polyglot/translate :ui/report-issue)
                     :style {:position :absolute
                             :top      "1em"
                             :right    "1em"}} "bug"]]
     (into [elem params] (first content))]))

(defn parameterlist [elem]
  (let [show-parameterlist? @(re-frame/subscribe [::show-parameterlist?])
        params              @(re-frame/subscribe [::params elem])
        toggle-param        (fn [param] #(re-frame/dispatch [::toggle-param elem param]))]
    [layout/vertically {:background  "rgb(250,250,250)"
                        :gap?        false
                        :fill?       true
                        :width       1
                        :scrollable? true
                        :style       {:border-bottom "solid 1px rgb(235,235,235)"
                                      :height        "35rem"
                                      :max-height    "35rem"}}
     (let [items (->> params
                      (map (fn [param]
                             (case (keyword (:describe param))
                               :boolean? {:id    (name (:spec param))
                                          :value (:name param)
                                          :label [layout/horizontally {:gap? false}
                                                  [:span (:name param)]
                                                  [layout/fill]
                                                  [element/toggle {:checked   (:value param)
                                                                   :on-change (toggle-param (:name param))} " "]]}
                               nil))))]
       [element/collection {:collapsable      true
                            :on-toggle-expand #(re-frame/dispatch [::toggle-parameterlist %])}
        (into [{:id    0
                :value " parameters"
                :label [layout/horizontally {:gap? false}
                        [element/icon (str "toggle" (when show-parameterlist? "-filled"))]
                        [:span "Parameters"]]}] items)])]))

(defn- formatted-doc [elem]
  (let [literal-elem (subs (str elem) 2)
        element-name (name (keyword literal-elem))]
   [layout/vertically {:fill? true}
    [element/article
     (str "# " element-name)
     (:doc (meta elem))]]))

(defn interactive-doc [elem]
  (letfn [(view []
            [layout/vertically {:background :white
                                :gap?       false
                                :compact?   true
                                :fill?      true}
             [layout/horizontally {:gap?     false
                                   :compact? true
                                   :fill?    true}
              [preview elem]
              [parameterlist elem]]
             [formatted-doc elem]])]
    #?(:clj view
       :cljs (reagent/create-class
              {:display-name         "interactive-doc"
               :component-will-mount #(re-frame/dispatch [::exercise-spec elem])
               :reagent-render       view}))))

(defn documentation []
  [interactive-doc #'ui.element.button.views/button])

