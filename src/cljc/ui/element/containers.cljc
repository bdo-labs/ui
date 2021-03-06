(ns ui.element.containers
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [garden.units :as unit]
            [garden.color :as color]
            [ui.util :as util]))


(defn style [theme]
  [[:.Flex {:flex 1}]
   #_[".Container:not(.layout-vertically):not(.Compact) > * + *" {:margin-left (unit/rem 1)}]
   #_[".Container.layout-vertically:not(.Compact) > * + *" {:margin-top (unit/rem 1)}]
   [:.Container {:flex-grow   1
                 :flex-shrink 0
                 :flex-basis  :auto
                 :box-sizing  :border-box
                 :display     :flex}
    [:&.hide {:display :none}]
    [:&.gap {:padding (unit/rem 2)}]
    [:&.wrap {:flex-wrap :wrap}]
    ;; [(selector/& (selector/not :.layout-vertically)) {:flex-direction :row}]
    ;; [(selector/& (selector/not :.No-wrap)) {:flex-wrap :wrap}]
    ;; [(selector/& (selector/not :.No-gap)) {:padding (unit/rem 2)}]
    ;; [(selector/& (selector/not (selector/attr-contains :class "Align"))) {:align-items :flex-start}]
    ;; [(selector/& (selector/not (selector/attr-contains :class "Justify"))) {:justify-content :flex-start}]
    [:&.layout-vertically {:flex-direction :column}
     [:&.fill {:height (unit/percent 100)}]]
    [:&.align-start-start {:justify-content :flex-start
                           :align-items     :flex-start}]
    [:&.align-start-end {:justify-content :flex-start
                         :align-items     :flex-end}]
    [:&.align-start-center {:justify-content :flex-start
                            :align-items     :center}]
    [:&.align-end-end {:justify-content :flex-end
                       :align-items     :flex-end}]
    [:&.align-end-start {:justify-content :flex-end
                         :align-items     :flex-start}]
    [:&.align-end-center {:justify-content :flex-end
                          :align-items     :center}]
    [:&.align-center-center {:justify-content :center
                             :align-items     :center}]
    [:&.align-center-start {:justify-content :center
                            :align-items     :flex-start}]
    [:&.align-center-end {:justify-content :center
                          :align-items     :flex-end}]
    [:&.space-between {:justify-content :space-between}]
    [:&.space-around {:justify-content :space-around}]
    [:&.space-none {:align-items :stretch}]
    [:&.scrollable {:min-height :min-content
                    :overflow   :auto}]
    ;; TODO https://github.com/noprompt/garden/issueselector/127
    #_[(selector/& :.Container (selector/> (selector/not :.Compact) (selector/+ :* :*))) {:margin-left (unit/rem 2)}]
    [#{:&.fill :.fill} {:box-sizing :border-box
                       :flex       1
                       :min-width  0
                       :min-height 0
                       :width      (unit/percent 100)}]
    [:&.inline {:display :inline-flex}]
    [:&.rounded {:border-radius (unit/rem 1)}]
    [:&.raised {:box-shadow [[0 (unit/rem 0.2) (unit/rem 0.3) (color/rgba [35 35 35 0.2])]]
                :overflow :auto}]]])


;; Parameter specifications
(spec/def ::background (spec/or :str string?
                                :key keyword?))
(spec/def ::compact? boolean?)
(spec/def ::fill? boolean?)
(spec/def ::gap? boolean?)
(spec/def ::inline? boolean?)
(spec/def ::raised? boolean?)
(spec/def ::rounded? boolean?)
(spec/def ::wrap? boolean?)
(spec/def ::scrollable? boolean?)
(spec/def ::align (spec/coll-of ::alignment :min-count 1 :max-count 2))
(spec/def ::alignment #{:start :end :center})
(spec/def ::layout #{:horizontally :vertically})
(spec/def ::space #{:between :around :none})
(spec/def ::content-type (spec/or :nil nil? :seq seq? :fn fn? :str string? :vec vector?))
(spec/def ::variable-content (spec/* ::content-type))
(spec/def ::width
  (spec/or :flex int?
           :width string?))


;; Consolidated parameters
(spec/def ::container-params
  (spec/keys :opt-un [::compact? ::fill? ::gap? ::inline? ::raised? ::rounded? ::wrap? ::scrollable?
                      ::layout ::background ::align ::space ::width]))

;; Full arguments specifications
(spec/def ::container-args
  (spec/cat :params (spec/? ::container-params)
            :content ::variable-content))


(defn container [& args]
  (let [{:keys [params content]}                              (util/conform! ::container-args args)
        ui-params                                             (util/keys-from-spec ::container-params)
        params                                                (merge {:gap?  true
                                                                      :wrap? true
                                                                      :align [:start :start]} params)
        {:keys [style background width scrollable? rounded?]} params
        background                                            (if-not (nil? background) {:background (last background)} {})
        class                                                 (util/params->classes (dissoc params :width :background))
        width                                                 (apply hash-map width)
        style                                                 (merge style width background)]
    (if (and scrollable? rounded?)
      [:div.Container
       (merge {:class class}
              (apply dissoc params (conj ui-params :class))
              {:style style})
       (into [:div.Container.fill.no-gap]
             (map last content))]
      (into [:div.Container
             (merge {:class class}
                    (apply dissoc params (conj ui-params :class))
                    {:style style})]
            (map last content)))))


(defn sidebar
  "
  Sidebar
  Wrap content in a sidebar"
  ([sidebar-content main-content]
   [sidebar {} sidebar-content main-content])
  ([{:keys [locked open backdrop ontop to-the on-click-outside] :as params} sidebar-content main-content]
   (fn [{:keys [locked open backdrop ontop to-the on-click-outside]} sidebar-content main-content]
     (let [classes (util/names->str [(when (true? open) :Open)
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


(defn card
  "
  Card
  A container with visual boundaries
  "
  [params & content]
  (if-not (map? params)
    [card {} params content]
    (fn []
      (let [classes     (concat [] (when (not-empty (:class params)) (:class params)) [:Card])
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
    (let [classes (merge [:Header (case (:size params) "large" :Large :Small)]
                         (:class params))]
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
