(ns ui.docs.icons
  (:require [ui.elements :as element]
            [ui.layout :as layout]))

(defn documentation []
  [layout/vertically {:background :white
                      :fill?      true
                      :gap?       false}
   [layout/centered {:class "demo"
                     :style {:position :relative
                             :max-height   "30rem"}}
    [element/icon {:size 15} "happy-outline"]]
   [element/article
    "## Icons
   #### Add some Flare with a Decent set of Symbols

   `ui` is compatible with most icon-fonts available through the same  
   construct. You give it a font-name and an icon-name and it will  
   find out how to render it. You will also need to include the  
   icon-font in your projects `index.html`.  
     
   *Note that you can set a default icon-font for your entire project by dispatching `icon-font` with the name of your font*
   "]])
