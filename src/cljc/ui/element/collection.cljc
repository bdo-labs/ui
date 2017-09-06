(ns ui.element.collection
  (:require [clojure.spec :as spec]
            [clojure.string :as str]
            [ui.util :as util :refer [=i]]))


(spec/def ::collection-params
  (spec/keys :req-un [::items]
             :opt-un [::show? ::select?]))


(spec/def ::element instance?)


(spec/def ::collection-args
  (spec/cat :params ::collection-params
            :element ::element))


(defn collection
  [{:keys [on-click on-mouse-enter predicate]}]
  (fn [{:keys [items show class select]} element]
    (let [items (->> items
                     (sort #(compare (last %1) (last %2))))
          bold-match #(if (=i % show) [:strong %] [:span %])]
      [:div {:class (util/names->str class)}
       [:ul.Collection {:ref #(reset! element %)}
        (when (not-empty items)
          (map-indexed (fn [n {:keys [id text] :as item}]
                         (let [text (if-not (= "" show)
                                      (let [pattern #?(:clj (re-pattern (str "(?iu)" (str/lower-case show)))
                                                       :cljs (js/RegExp. (str/lower-case show) "ig"))]
                                        (str/replace text pattern #(str ":" %1 ":")))
                                      text)]
                           (into [:li {:key            (str "item-" n)
                                       :value          id
                                       :on-mouse-enter #(on-mouse-enter n)
                                       :class          (when (= select n) "Selected")
                                       :on-click       #(when (fn? on-click)
                                                          (on-click item))}]
                                 (->> (str/split text #":")
                                      (map bold-match)))))
                       items))]])))


(spec/fdef collection
        :args ::collection-args
        :ret vector?)

