(ns ui.element.link
  (:require #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [clojure.string :as str]
            [re-frame.core :as re-frame]
            [ui.util :as u]))


(defn link
  [{:keys [action active? class]
    :as   params} content]
  (let [classes  (u/names->str (conj [(when active? :Active)] class))
        on-click #(re-frame/dispatch action)]
    [:a.Link
     (merge
      (dissoc params :action :class)
      {:class    classes
       :on-click on-click}) content]))
