(ns ui.element.containers
  (:require [ui.util :refer [names->str]]
            [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as spec]
            [garden.color :as color]
            [ui.util :as u]))


(defn container
  "
  Container
  Containers ease the process of creating robust layout's.
  "
  [params & content]
  (fn [params & content]
    (if-not (map? params)
      [container {} params content]
      (let [{:keys [raised? rounded? direction align justify compact no-gap no-wrap class on-click style fill]} params
            classes (names->str (concat [:Container
                                         (if (not-empty direction)
                                           (if (= "column" direction) "Vertically" "Horizontally")
                                           "Horizontally")
                                         (when (not-empty align) (str "Align-" align))
                                         (when (not-empty justify) (str "Justify-" justify))
                                         (when rounded? :Rounded)
                                         (when raised? :Raised)
                                         (when (true? fill) :Fill)
                                         (when (true? no-gap) :No-gap)
                                         (when (true? no-wrap) :No-wrap)
                                         (when (true? compact) :Compact)]
                                        class))]
        [:div (merge (dissoc params :direction :align :justify :fill :no-gap :no-wrap :compact :rounded? :raised?)
                     {:style style :class classes :on-click on-click})
         (map-indexed #(with-meta %2 {:key (str "content-" %1)}) content)]))))


(spec/def ::direction #{"horizontally" "vertically"})
(spec/def ::align #{"start" "end" "center" "stretch"})
(spec/def ::justify #{"start" "end" "center" "space-between" "space-around"})
(spec/def ::compact boolean?)
(spec/def ::no-gap boolean?)
(spec/def ::fill boolean?)
(spec/def ::content vector?)


(spec/def ::container-args
  (spec/keys
   :req-un [::content]
   :opt-un [::direction ::align ::justify ::compact ::no-gap ::fill]))


(spec/fdef container :args ::container-args :ret vector?)
;; (spec/describe ::container-args)
;; (spec/exercise-fn container 3)


(defn sidebar
  "
  Sidebar
  Wrap content in a sidebar"
  ([sidebar-content main-content]
   [sidebar {} sidebar-content main-content])
  ([{:keys [locked open backdrop ontop to-the on-click-outside] :as params} sidebar-content main-content]
   (fn [{:keys [locked open backdrop ontop to-the on-click-outside]} sidebar-content main-content]
     (let [classes (names->str [(when (true? open) :Open)
                                (when (true? ontop) :Ontop)
                                (when (true? locked) :Locked)
                                (if (not= "right" to-the) :Align-left :Align-right)])]
       [:div.Sidebar {:class classes}
        [:div.Slider
         (when (not= "right" to-the)
           [:sidebar sidebar-content])
         (when (true? backdrop)
           [:div.Backdrop {:on-click on-click-outside}])
         [:main main-content]
         (when (= "right" to-the)
           [:sidebar sidebar-content])]]))))


(spec/def ::open boolean?)
(spec/def ::ontop boolean?)
(spec/def ::backdrop boolean?)
(spec/def ::locked boolean?)
(spec/def ::on-click-outside fn?)
(spec/def ::to-the #{"left" "right"})
(spec/def ::sidebar-args (spec/keys :opt-un [::open ::ontop ::locked ::backdrop ::to-the ::on-click-outside]))

;; (spec/fdef sidebar :args ::sidebar-args :ret vector?)
;; (spec/describe ::sidebar-args)
;; (spec/exercise-fn sidebar 3)


(defn card
  "
  Card
  A container with visual boundaries
  "
  [params & content]
  (if-not (map? params)
    [card {} params content]
    (fn []
      (let [classes (concat [] (when (not-empty (:class params)) (:class params)) [:Card])
            params-list (merge params {:class classes})]
        [container params-list
         (map-indexed #(with-meta %2 {:key (str "card-" %1)}) content)]))))


(defn header
  "
  Header
  "
  [params & content]
  (if-not (map? params)
    [header {} params content]
    (let [classes [:Header (case (:size params) "large" :Large :Small)]]
      [container
       (merge params {:class   classes
                      :align   "center"
                      :justify "space-between"
                      :no-gap  true})
       (map-indexed
        #(with-meta %2 {:key (str "header-" %1)})
        content)])))


(defn code
  "
  Code
  Used for producing nicely formatted and syntax-highlighted
  code-blocks.
  "
  ([params & content]
   (if-not (map? params)
     [code {} content]
     [:div.Code params content])))
