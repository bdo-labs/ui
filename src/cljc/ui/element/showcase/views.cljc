(ns ui.element.showcase.views
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.spec-helper :as spec-helper]
            [clojure.tools.reader :refer [read-string]]
            [clojure.spec.alpha :as spec]
            [ui.util :as util :refer [deref?]]
            [clojure.string :as str]
            [re-frame.core :as re-frame]))

;; Events -----------------------------------------------------------------

(re-frame/reg-event-db
 ::showcase
 (fn [db [k id specification]]
   (let [element                 (str/replace (str (namespace specification)) #"(\.spec|ui\.)" "")
         {:keys [req-un opt-un]} (spec-helper/extract-spec-keys (nth (spec/form specification) 2))
         showcase                {:spec        specification
                                  :element     element
                                  :show-source false
                                  :require     req-un
                                  :optional    opt-un
                                  :state       (vec (vals (last (last (spec/exercise specification)))))}]
     (assoc-in db [k id] showcase))))

(re-frame/reg-event-db
 ::toggle-source
 (fn [db [_ id]]
   (update-in db [::showcase id :show-source] not)))

(re-frame/reg-event-db
 ::set-controller
 (fn [db [_ id controller value]]
   (assoc-in db [::showcase id :state 0 controller] value)))

(re-frame/reg-event-db
 ::toggle-controller
 (fn [db [_ id controller]]
   (update-in db [::showcase id :state 0 controller] not)))

;; Subscriptions ----------------------------------------------------------

(re-frame/reg-sub
 ::spec
 (fn [db [_ id]]
   (get-in db [::showcase id :spec])))

(re-frame/reg-sub
 ::state
 (fn [db [_ id]]
   (get-in db [::showcase id :state])))

(re-frame/reg-sub ::required (fn [db [k id]] (get-in db [::showcase id :required])))
(re-frame/reg-sub ::optional (fn [db [k id]] (get-in db [::showcase id :optional])))
(re-frame/reg-sub ::element (fn [db [k id]] (get-in db [::showcase id :element])))

(re-frame/reg-sub
 ::controller
 (fn [db [_ id controller]]
   (get-in db [::showcase id :state 0 controller])))

(re-frame/reg-sub
 ::show-source
 (fn [db [_ id]]
   (get-in db [::showcase id :show-source])))

;; Views ------------------------------------------------------------------

(defn- source
  "Display formatted source-code."
  ;; TODO Replace this with a robust highlighter that just takes the raw code
  [id]
  (let [state            @(re-frame/subscribe [::state id])
        element          @(re-frame/subscribe [::element id])
        show-source?     @(re-frame/subscribe [::show-source id])
        toggle-source    #(re-frame/dispatch [::toggle-source id])
        [params content] state
        spacing          (apply str (repeat (+ 3 (count element)) " "))]
    [layout/horizontally {:gap? false :fill? true}
     [layout/vertically {:gap? false :width 7}
      (when show-source?
        [:div.Code {:style {:width "100%"}}
         [:span.Parens "["]
         [:span.Symbol (str element " ")]
         [:span.Parens "{"]
         (let [[k v] (first params)]
           [:span
            [:span.Keyword (str k) " "]
            [:span.Symbol (pr-str v) "\n"]])
         (when (> (count params) 2)
           (for [[k v] (drop-last (rest params))]
             [:span
              [:span.Keyword (str spacing k) " "]
              [:span.Symbol (pr-str v) "\n"]]))
         (when (>= (count params) 2)
           (into [:span]
                 (let [[k v] (last params)]
                   [[:span.Keyword (str spacing k) " "]
                    [:span.Symbol (pr-str v)]])))
         [:span.Parens "}"]
         [:span " " content]
         [:span.Parens "]"]])]
     [layout/vertically {:gap? false :align [:end :end] :width 3}
      [element/button {:style {:border 0} :flat true :circular true :on-click toggle-source}
       [element/icon "code"]]]]))

(defn value->model [s]
  (if (deref? s) s (atom (str s))))

(defn- type->input
  [id prop type]
  (let [key       (util/slug "state" prop)
        value     @(re-frame/subscribe [::controller id prop])
        on-toggle (fn [prop] #(re-frame/dispatch [::toggle-controller id prop]))
        on-edit   (fn [prop] #(re-frame/dispatch [::set-controller id prop (.-value (.-target %))]))
        model (value->model value)]
    [:div
     (case type
       :boolean? [element/toggle {:key key :on-change (on-toggle prop) :checked value} (name prop)]
       :ratom?   [element/textfield {:key key :model model :label (str prop)}]
       [element/textfield {:key key :model model :label (str prop)}])]))

(defn- output-controllers
  [id un]
  (let [type        #(keyword (spec/describe %))
        controllers (->> un (group-by type))]
    [:div
     (doall
      (for [[k v] controllers]
        (into [layout/vertically {:key (str "controller" k)}]
              (->> v (map (fn [x] (let [t (if (not (nil? (type x))) (type x) (:type (meta x)))]
                                    [type->input id (keyword (name x)) t])))))))]))

(defn showcase
  [item specification]
  (let [id            (str item)
        state         (re-frame/subscribe [::state id])
        required      (re-frame/subscribe [::required id])
        optional      (re-frame/subscribe [::optional id])]
    (re-frame/dispatch-sync [::showcase id specification])
    (fn []
      [layout/vertically {:fill? true}
       [element/section (str (:doc (meta item)))]
       [layout/horizontally {:fill? true :gap? false :space :between}
        [layout/vertically {:gap? false :width 6}
         ;; Preview
         [layout/centered {:fill? true :class "demo"}
          (into [item] @state)]
         ;; Source
         [source id]]
        ;; Controllers
        [layout/vertically {:gap? false :width 2}
         (when (some? @required)
           [output-controllers id @required])
         (when (some? @optional)
           [output-controllers id @optional])]]])))
