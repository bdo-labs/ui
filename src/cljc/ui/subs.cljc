(ns ui.subs
  (:require [re-frame.core :as re-frame :refer [reg-sub]]
            #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [ui.element.numbers.subs]
            [ui.util :as u]))


(defn extract
  "Extracts [key] from [db]"
  [db [key]]
  (-> db key))


(reg-sub :active-doc-item extract)
(reg-sub :active-panel extract)
(reg-sub :icon-font extract)


(reg-sub :progress
         (fn [db [key]]
           (or (-> db key) 0)))
