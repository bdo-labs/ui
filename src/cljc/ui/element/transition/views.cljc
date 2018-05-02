(ns ui.element.transition.views
  (:require [clojure.string :as str]
            [ui.element.transition.spec :as spec]
            [ui.util :as util]))

(defn- get-animation-style [animation]
  (reduce (fn [out [k v]]
            (assoc out (keyword (str "animation-" (name k))) v))
          {} animation))

(defn transition
  [& args]
  (let [{:keys [params]} (util/conform! ::spec/args args)
        {:keys [id]
         :or   {id (util/gen-id)}} params]
    (fn [& args]
      (let [{:keys [params body]} (util/conform! ::spec/args args)
            {:keys [model animation]} params]
        [:div.Transition {:key (util/slug "transition" id)
                          :class @model
                          :style (get-animation-style animation)}
         body]))))
