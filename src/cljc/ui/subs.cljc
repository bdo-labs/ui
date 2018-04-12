(ns ui.subs
  (:require [re-frame.core :as re-frame]
            [ui.util :as util]))


(re-frame/reg-sub :fragments util/extract)
(re-frame/reg-sub :active-doc-item util/extract)
(re-frame/reg-sub :active-panel util/extract)
(re-frame/reg-sub :icon-font util/extract)
