(ns ui.element.containers.views
  (:require [ui.element.containers.spec :as spec]
            [clojure.string :as str]
            [ui.util :as util]))

(defn container
  "
  Container
  "
  [& args]
  (let [{:keys [params content]} (util/conform! ::spec/container-args args)
        ui-params                (util/keys-from-spec ::spec/container-params)
        params                   (merge {:gap? true :wrap? true :align [:start :start]} params)
        class                    (util/params->classes (dissoc params :width :background))
        style                    (-> (:style params)
                                     (merge (select-keys params [:background]))
                                     (merge (apply hash-map (:width params))))]
    (into [:div.Container (merge (apply dissoc params ui-params) {:class class :style style})]
          (if (every? true? ((juxt :scrollable? :rounded?) params))
            [(into [:div.Container.fill.no-gap] content)]
            content))))

(defn card
  "
  Card
  A container with visual boundaries
  "
  [params & content]
  (if-not (map? params)
    [card {} params content]
    (let [default-params {:raised? true :class "Card" :scrollable? true}
          params         (merge default-params params)]
      [container params content])))

(defn header
  "
  Header
  "
  [params & content]
  (if-not (map? params)
    [header {} params content]
    (let [class (str/join " " ["Header" (case (:size params) "large" "large" "small") (:class params)])
          params  (merge params {:class   class
                                 :align   [:center :center]
                                 :space :between
                                 :gap?  false})]
      (into [container params] content))))

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
