(ns ui.docs.notification
  (:require #?(:cljs [reagent.core :refer [atom]])
            [ui.element.notification.spec :as spec]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.element.showcase.views :refer [showcase]]))


(defn documentation []
  (let [text-input1 (atom "")
        text-input2 (atom "")]
    (fn []
      [layout/vertically {:background  :white
                          :scrollable? true}
       [element/article
        "### Notification(s)

Show notifications based on outside input

```clojure
(ns your.namespace
  (:require [re-frame.core :as re-frame]
            [ui.elements :as element]))

(def input1 (atom \"\")
(def input2 (re-frame/subscribe [:listen.to/this]))

;; use a textfield to manipulate input1
[element/textfield {:model input1 :placeholder \"Write text here to have it show up in the notification below\"}]

;; show a single notification
[element/notification {:model input1}]

;; show multiple notifications
[element/notifications {:model [input1 input2]}]
```
       "
        [element/textfield {:model text-input1 :placeholder "Write text here to have it show up in the notification"}]
        [:br]
        [element/textfield {:model text-input2 :placeholder "Write text here to have it show up among notifications"}]
        "Notification element below"
        [element/notification {:model text-input1}]

        "Notification**s** element below"
        [element/notifications {:model [text-input1 text-input2]}]]])))
