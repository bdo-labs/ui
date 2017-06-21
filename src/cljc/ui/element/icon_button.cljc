(ns ui.element.icon-button
  (:require [ui.element.button :refer [button]]
            [ui.element.icon :refer [icon]]))


(defn icon-button
  ([icon-name]
   [icon-button {} icon-name])
  ([{:keys [class font] :as params} icon-name]
   [button (merge {:class (concat [:Icon] class)}
                  (dissoc params
                          :class
                          :font))
    " "
    [icon {:font font} icon-name]]))
