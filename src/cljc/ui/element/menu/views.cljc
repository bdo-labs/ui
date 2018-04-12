(ns ui.element.menu.views
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            #?(:cljs [reagent.dom])
            [ui.util :as util]
            [clojure.string :as str]
            [ui.element.menu.spec :as spec]
            [ui.element.containers.views :refer [container]]
            [reagent.core :as reagent]))

(defn dropdown [& args]
  (let [{:keys [params content]}   (util/conform! ::spec/args args)
        {:keys [id
                open?
                on-click-outside
                origin]
         :or   {id (util/gen-id)}} params
        ui-params                  (util/keys-from-spec ::spec/params)
        is-open?                   (atom open?)]
    (letfn [(on-click [element event]
              (when (and @is-open?
                         (ifn? on-click-outside)
                         (false? (.contains element (-> event .-target))))
                (on-click-outside element event)))
            (render-fn [& args]
                       (let [{:keys [params content]} (util/conform! ::spec/args args)
                             {:keys [open?]}  params
                             classes          (str "Dropdown "
                                                   (if open? "open " "not-open ")
                                                   (when origin (str "origin-" (str/join "-" (map name origin)))))
                             container-params {:key      (util/slug id "dropdown")
                                               :id       id
                                               :layout   :vertically
                                               :gap?     false
                                               :raised?  true
                                               :rounded? true
                                               :class    classes}
                             params           (merge container-params (apply dissoc params ui-params))]
                         (do (reset! is-open? open?)
                             (into [container params] content))))]
      #?(:clj render-fn
         :cljs (reagent/create-class
                {:display-name           "dropdown"
                 :component-did-mount    #(when-let [target (-> % (reagent.dom/dom-node))]
                                            (.addEventListener js/document "click" (partial on-click target)))
                 :component-will-unmount #(when-let [target (-> % (reagent.dom/dom-node))]
                                            (.removeEventListener js/document "click" (partial on-click target)))
                 :reagent-render         render-fn})))))

