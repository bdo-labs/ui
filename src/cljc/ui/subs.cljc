(ns ui.subs
  (:require [re-frame.core :as re-frame :refer [reg-sub]]
            #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [ui.element.numbers.subs]
            [ui.util :as u]))


(reg-sub :active-doc-item u/extract)
(reg-sub :active-panel u/extract)
(reg-sub :icon-font u/extract)
(reg-sub :progress [u/extract (fn [progress] (or progress 0))])
