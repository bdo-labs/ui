(ns ui.element.documentation.views
  (:require [ui.elements :as element]
            [ui.layout :as layout]
            [clojure.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            #?(:cljs [reagent.core :as reagent])
            [clojure.walk :as walk]
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
         params        (apply hash-map (interleave (map :spec params) (map identity params)))
         gen           (assoc-in temp-gen [:params] params)]
     (assoc-in db [::state (elem-path elem)] gen))))

(re-frame/reg-event-db
 ::toggle-param
 (fn [db [k elem param]]
   (update-in db [::state (elem-path elem) :params param :value] not)))

(re-frame/reg-event-db
 ::set-param
 (fn [db [k elem param value]]
   (assoc-in db [::state (elem-path elem) :params param :value] value)))

;; Subscriptions ----------------------------------------------------------

(re-frame/reg-sub ::show-param-list? util/extract)

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
   (get-in state [:params] {})))

(re-frame/reg-sub
 ::params-map
 (fn [[k elem]]
   (re-frame/subscribe [::params elem]))
 (fn [params]
   (if-let [params-map (some->> params
                                (vals)
                                (map (fn [param]
                                        (when (and (seq param)
                                                   (some? (:value param)))
                                          {(keyword (:name param)) (:value param)})))
                                (remove nil?)
                                (apply merge))]
     params-map
     {})))

(re-frame/reg-sub
 ::content
 (fn [[k elem]]
   (re-frame/subscribe [::state elem]))
 (fn [state [k elem content]]
   (if (seq content)
     content
     (get-in state [:content] nil))))

;; Views ------------------------------------------------------------------

(defn- preview [elem & [content]]
  (let [params    @(re-frame/subscribe [::params-map elem])
        content   @(re-frame/subscribe [::content elem content])
        issue-url "https://github.com/bdo-labs/ui/issues/new"]
    [layout/centered {:class "demo"
                      :width 2
                      :fill? true
                      :style {:position :relative
                              :height   "100%"}}
     [:a.face-tertiary {:href   issue-url
                        :target :_blank}
      [element/icon {:title (polyglot/translate :ui/report-issue)
                     :style {:position :absolute
                             :top      "1em"
                             :right    "1em"}} "bug"]]
     (when (seq content)
       [elem params content])]))

(defn- param-list [elem]
  (let [params            @(re-frame/subscribe [::params elem])
        toggle-param      (fn [param] #(re-frame/dispatch [::toggle-param elem (:spec param)]))
        set-param         (fn [param] #(re-frame/dispatch [::set-param elem (:spec param) (str %1)]))]
    [layout/vertically {:background  "rgb(250,250,250)"
                        :gap?        false
                        :fill?       true
                        :width       1
                        :scrollable? true
                        :style       {:border-bottom "solid 1px rgb(235,235,235)"
                                      :height        "100%"}}
     (let [items (->> params
                      (map (fn [[sp param]]
                             (let [literal-spec (subs (str sp) 1)]
                               (case (keyword (:describe param))
                                 :boolean? {:id    literal-spec 
                                            :value (:name param)
                                            :label [layout/horizontally {:gap? false}
                                                    [:span (:name param)]
                                                    [layout/fill]
                                                    [element/toggle {:checked   (:value param)
                                                                     :on-change (toggle-param param)} " "]]}
                                 {:id    literal-spec
                                  :value (:name param)
                                  :label [layout/horizontally {:gap? false}
                                          [element/textfield {:on-change (set-param param)
                                                              :required (:required param)
                                                              :value     (str (:value param))
                                                              :label     (:name param)}]]})))))]
       (when (seq items)
         [element/collection {} items]))]))

(defn- formatted-doc [elem]
  (let [literal-elem (subs (str elem) 2)
        element-name (name (keyword literal-elem))]
    [element/article
     (str "# " element-name)
     (:doc (meta elem))]))

(defn documentation [elem & [content]]
  (letfn [(view []
            [layout/vertically {:background :white
                                :gap?       false
                                :compact?   true
                                :fill?      true}
             [layout/horizontally {:gap?     false
                                   :compact? true
                                   :fill?    true
                                   :style {:max-height "22em"}}
              [preview elem content]
              [param-list elem]]
             [layout/vertically {:fill? true}
              [formatted-doc elem]]])]
    #?(:clj view
       :cljs (reagent/create-class
              {:display-name         "interactive-doc"
               :component-will-mount #(re-frame/dispatch [::exercise-spec elem])
               :reagent-render       view}))))
