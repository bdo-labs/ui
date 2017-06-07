(ns ui.subs
  (:require [re-frame.core :as re-frame :refer [reg-sub]]
            [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as spec]
            [ui.element.numbers.subs]
            [ui.util :as u]))


(defn extract
  "Extracts [key] from [db]"
  [db [key]]
  (-> db key))


(reg-sub :active-doc-item extract)
(reg-sub :active-panel extract)
(reg-sub :icon-font extract)
