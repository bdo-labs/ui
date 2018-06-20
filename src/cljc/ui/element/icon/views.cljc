(ns ui.element.icon.views
  (:require [ui.element.icon.spec :as spec]
            [clojure.string :as str]
            [ui.util :as util]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub :ui/icon-font util/extract)

(re-frame/reg-event-fx
 :init-icons
 (fn [{:keys [db]}]
   {:dispatch [:ui/icon-font "ion"]
    :db db}))

(re-frame/reg-event-db
 :ui/icon-font
 (fn [db [k font]]
   (let [font (apply hash-map (util/conform! ::spec/font font))]
     (assoc db k font))))

(defn icon
  "#### Add some Flare with a Decent set of Symbols

   `ui` is compatible with most icon-fonts available through the same  
   construct. You give it a font-name and an icon-name and it will  
   find out how to render it. You will also need to include the  
   icon-font in your projects `index.html`.  
     
   *Note that you can set a default icon-font for your entire project by dispatching `icon-font` with the name of your font*"
  [& args]
  (let [{:keys [params content]} (util/conform! ::spec/args args)
        {:keys [style font size color]
         :or   {font  @(re-frame/subscribe [:ui/icon-font])
                style {}}}     params
        style                 (merge style
                                     (when (some? size) {:font-size (str size "rem")})
                                     (when (some? color) {:color color}))
        class                 (:class params)
        params                (dissoc params :size :font :color :class :style)
        font                  (if (vector? font) (apply hash-map font) font)]
    (if-let [font-name (:font-name font)]
      [:i.Icon (merge {:class (str font-name " " class)
                       :style style}
                      params) content]
      (let [font-prefix (:font-prefix font)
            class       (str/join " " (conj [font-prefix (str font-prefix "-" content)] class))]
        [:i.Icon (merge {:class class
                         :style style}
                        params)]))))
