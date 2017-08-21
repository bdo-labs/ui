(ns ui.element.auto-complete
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [clojure.spec :as spec]
            [clojure.string :as str]
            [clojure.core.async :refer [<! timeout #?(:clj go)]]
            #?(:cljs [reagent.core :refer [atom]])
            [ui.element.collection :refer [collection]]
            [ui.element.label :refer [label]]
            [ui.element.textfield :refer [textfield]]
            [ui.util :as u :refer [=i]]))


(spec/def ::auto-complete-params
  (spec/merge (spec/keys :req-un [::items]
                         :opt-un [::multiple? ::close-on-select?])
              ::textfield-params))


(spec/def ::auto-complete-args
  (spec/cat :params ::auto-complete-params))


(defn auto-complete
  [{:keys [on-change on-select on-remove predicate]
    :or   {predicate str/starts-with?}}]
  (let [!coll-el           (clojure.core/atom nil)
        open?              (atom false)
        query              (atom "")
        select             (atom -1)
        selected           (atom {})
        on-textfield-click #(reset! open? true)
        on-mouse-enter     #(reset! select %)
        on-blur            #(go (<! (timeout 200))
                                (when @open? (reset! open? false)))]
    (fn [{:keys [items placeholder multiple? disabled? focus? read-only? close-on-select?]
         :or   {multiple? false disabled? false read-only? false focus? false}}]
      (let [filter-fn          #(predicate (str/upper-case %) (str/upper-case @query))
            items              (->> items (filter #(filter-fn (:text %))))
            on-internal-change #(do (reset! query (.-value (.-target %)))
                                    (when (fn? on-change)
                                      (if multiple?
                                        (on-change (vals @selected))
                                        (on-change (.-value (.-target %))))))
            on-click           #(do (swap! selected assoc (:id %) %)
                                    (if multiple?
                                      (reset! query "")
                                      (reset! query (:text %)))
                                    (when (fn? on-select) (on-select %)))]
        ;; Enable scroll using keyboard
        ;; REVIEW Could this be cleaned up & generalized?
        (letfn [(scroll [f]
                  #?(:cljs (when (and (> @select 3)
                                      (< @select (- (count items) 3)))
                             (when-let [element @!coll-el]
                               (let [scroll-top    (.-scrollTop element)
                                     scroll-height (.-scrollHeight element)
                                     item-height   (* 2 (js/parseInt (.-height (js/getComputedStyle (.-firstElementChild element)))))
                                     jump          (* (f 0) item-height)]
                                 (when (and (>= scroll-top 0)
                                            (<= scroll-top scroll-height))
                                   (set! (.-scrollTop element) (+ scroll-top jump))))))))]
          (let [on-key-down #(case (u/code->key (.-which %))
                               "down"           (do (reset! select (min (dec (count items)) (inc @select)))
                                                    (scroll inc))
                               "up"             (do (reset! select (max 0 (dec @select)))
                                                    (scroll dec))
                               "enter"          (let [selected (nth items @select)]
                                                  (reset! query (:text selected))
                                                  (when (fn? on-select) (on-select selected))
                                                  false) true)]
            [:div.Auto-complete {:class (u/names->str [(when disabled? :Disabled)
                                                       (when read-only? :Read-only)])}
             ;; Input-handling
             [textfield {:placeholder placeholder
                         :value       @query
                         :focus?      focus?
                         :disabled?   disabled?
                         :read-only?  read-only?
                         :on-key-down on-key-down
                         :on-blur     on-blur
                         :on-change   on-internal-change
                         :on-click    on-textfield-click}]
             ;; Filtered list of items
             (when @open?
               [collection
                (merge {:items     items
                        :show      @query
                        :predicate predicate}
                       (when (and (not disabled?)
                                  (not read-only?))
                         {:select         @select
                          :on-mouse-enter on-mouse-enter
                          :on-click       on-click}))
                !coll-el])
             ;; Selected elements
             (when multiple?
               [:div.Labels (for [lbl (vals @selected)]
                              (let [on-key-down #(case (.-which %)
                                                   8 (do (swap! selected dissoc (:id lbl))
                                                         (when (fn? on-remove) (on-remove %))))]
                                ^{:key (:id lbl)} [label (merge lbl {:on-key-down on-key-down})]))])]))))))


#_(spec/fdef auto-complete
        :args ::auto-complete-args
        :ret vector?)
