(ns ui.element.menu.views
  (:require [ui.util :as util]
            [clojure.string :as str]
            [ui.element.menu.spec :as spec]
            [ui.element.containers.views :refer [container]]))

(defn dropdown [& args]
  (let [{:keys [params content]}   (util/conform! ::spec/dropdown-args args)
        {:keys [id
                open?
                origin]
         :or   {id (util/gen-id)}} params
        classes                    (str "Dropdown "
                                        (if open? "open " "not-open ")
                                        (when origin (str "origin-" (str/join "-" (map name origin)))))
        ui-params                  (util/keys-from-spec ::spec/dropdown-params)
        container-params           {:key      (util/slug id "dropdown")
                                    :layout   :vertically
                                    :gap?     false
                                    :raised?  true
                                    :rounded? true
                                    :class    classes}
        params                     (merge container-params (apply dissoc params ui-params))]
    (into [container params]
          (when open? (map last content)))))

