(ns ui.docs.boundary
  (:require [#?(:clj clojure.core :cljs reagent.core) :refer [atom]]
            [ui.elements :as element]
            [ui.layout :as layout]))


(defn documentation []
  (let [!event (atom nil)]
   [element/article
    "### Boundary

   Boundary as implied by it's name will find the boundary of it's
   immidiate child, so that you can apply some utility-actions that
   are not part of the regular DOM. The two most typical scenarios
   that comes to mind is:
   
   1. Being able to close a dropdown-menu when you click outside of it  
   2. Adding animation whenever the boundary is within the viewport  
   
   There are probably thousands of other ways to use these boundaries, 
   that was just meant to spark some nerves! 
   "
    [:pre (pr-str @!event)]
    #_[element/boundary {:on-mouse-within #(reset! !event :on-mouse-within)
                       :on-mouse-enter  #(reset! !event :on-mouse-enter)
                       :on-mouse-leave  #(reset! !event :on-mouse-leave)
                       :on-mouse-up     #(reset! !event :on-mouse-up)}
     [element/button {:fill? true} "Move mouse-pointer here"]]]))

