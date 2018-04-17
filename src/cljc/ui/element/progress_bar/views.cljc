(ns ui.element.progress-bar.views)

(defn progress-bar
  [{:keys [progress]}]
  [:div.Progress-bar
   [:div.Progress {:style {:width (str progress "%")}}]])
