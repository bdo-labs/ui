(ns ui.docs.icons
  (:require [ui.elements :as element]
            [ui.layout :as layout]
            [garden.color :as color]))

(defn documentation []
  [element/article
   "### Icons
   #### Add some Flare with a Decent set of Symbols

   `ui` is compatible with most icon-fonts available through the same  
   construct. You give it a font-name and an icon-name and it will  
   find out how to render it.  
        
   Note that you can set a default icon-font for your entire project  
   by dispatching `icon-font` with the name of your font.  
        
   Ex.  
   ```clojure
   (ui/dispatch [:icon-font \"ion\"])
   [element/icon {:size 15} \"happy-outline\"]
   [element/icon {:size 10} \"coffee\"]
   ```
   "
   [layout/centered
    [element/icon {:font "ion"
                   :size 15} "happy-outline"]
    [element/icon {:font "ion"
                   :size 10} "coffee"]]])
