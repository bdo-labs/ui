(ns ui.element.containers.views
  (:require [ui.element.containers.spec :as spec]
            [clojure.string :as str]
            [ui.util :as util]))

(defn container [& args]
  (let [{:keys [params content]}                              (util/conform! ::spec/container-args args)
        ui-params                                             (util/keys-from-spec ::spec/container-params)
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
