(ns ui.docs.centered
  (:require [ui.elements :as element]
            [ui.layout :as layout]))

(defn documentation
  []
  [layout/vertically {:background :white
                      :fill? true
                      :gap? false}
   [layout/centered {:class "demo"
                     :gap? false
                     :style {:height "22em"
                             :max-height "22em"}}
    [:div.Demo-box "Box"]
    [:div.Demo-box "Box"]
    [:div.Demo-box "Box"]]
   [element/article
    "
## centered

As the name suggests, this layout will center it's children  
vertically and horizontally dead-center.

```clojure
[layout/centered {:class \"demo\"}
  [:div.Demo-box \"Box\"]
  [:div.Demo-box \"Box\"]
  [:div.Demo-box \"Box\"]]
```
"]])
