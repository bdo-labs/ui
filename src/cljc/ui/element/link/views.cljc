(ns ui.element.link.views
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]
            [ui.util :as util]))

(defn link
  [{:keys [action active? class]
    :as   params} content]
  (let [classes  (util/names->str (conj [(when active? :Active)] class))
        on-click #(re-frame/dispatch action)]
    [:a.Link
     (merge
      (dissoc params :action :class)
      {:class    classes
       :on-click on-click}) content]))
