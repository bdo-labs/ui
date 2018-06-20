(ns ui.docs.vertically
  (:require [ui.elements :as element]
            [ui.layout :as layout]))

(defn documentation
  []
  [layout/vertically {:background :white :fill? true :gap? false}
   [layout/vertically {:class "demo"
                       :style {:height "22em"
                               :max-height "22em"}}
    [:div.Demo-box "Box"]
    [:div.Demo-box "Box"]
    [:div.Demo-box "Box"]]
   [element/article
    "
## vertically

By default in a vertical layout, the children are layed out top to
bottom, left to right.

```clojure
[layout/vertically {:class \"demo\"}
    [:div.Demo-box \"Box\"]
    [:div.Demo-box \"Box\"]
    [:div.Demo-box \"Box\"]]
```
"]])
