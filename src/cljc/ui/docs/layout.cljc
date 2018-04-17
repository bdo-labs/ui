(ns ui.docs.layout
  (:require [ui.elements :as element]))

(defn documentation []
  [element/article
   "
## Layout

Layouts uses `element/container` behind the scenes, but is prefered
over the container-element where applicable as your code will become
more readable and succinct because of some sane defaults.
Just keep in mind that layouts are written as verbs and you'll get
used to it really quickly.
"])
