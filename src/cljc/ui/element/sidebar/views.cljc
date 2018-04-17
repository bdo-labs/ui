(ns ui.element.sidebar.views
  (:require [ui.util :as util]
            [ui.element.sidebar.spec :as spec]))

(defn sidebar
  [& args]
  (let [{:keys [params
                sidebar-content
                main-content]}     (util/conform! ::spec/args args)
        {:keys [to-the
                backdrop?
                on-click-outside]} params
        class                      (util/params->classes (merge params {:to-the (or to-the :left)}))]
    [:div.Sidebar {:class class}
     [:div.Slider
      (when (not= :rigth to-the)
        [:sidebar sidebar-content])
      (when backdrop?
        [:div.Backdrop {:on-click on-click-outside}])
      [:main main-content]
      (when (= :right to-the)
        [:sidebar sidebar-content])]]))
