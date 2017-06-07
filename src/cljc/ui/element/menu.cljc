(ns ui.element.menu
  (:require [ui.util :as u]))


(defn dropdown
  [params & content]
  (if-not (map? params)
    [dropdown {} params content]
    (fn [params & content]
      (let [{:keys [open?]} params
            classes         (u/names->str [(when open? :Open)
                                           (:class params)])]
        [:menu.Dropdown
         (merge (dissoc params :open? :class) {:class classes})
         content]))))
