(ns ui.element.progress-bar.views)

(defn progress-bar
  [{:keys [progress align]
    :or   {align :top}}]
  [:div.Progress-bar {:class (keyword (str "align-" (name align)))}
   [:div.Progress {:style {:width (str progress "%")}}]])
