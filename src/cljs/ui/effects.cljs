(ns ui.effects
  (:require [accountant.core :as accountant]
            [re-frame.core :as re-frame]
            [clojure.string :as str]
            [cemerick.url :refer [url-encode]]
            [ui.util :as util]))


(re-frame/reg-fx
 :navigate-to
 (fn [fragments]
   (let [encoded (->> fragments
                      (map #(if (keyword? %) (name %) (str %)))
                      (map url-encode))
         url     (str "/" (str/join "/" encoded))]
     (accountant/navigate! url))))
