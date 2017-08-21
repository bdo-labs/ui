(ns ui.routes
  (:require [re-frame.core :as re-frame]
            [secretary.core :as secretary :refer-macros [defroute]]
            [accountant.core :as accountant]
            [ui.util :as util]))


(defroute "/" []
  (re-frame/dispatch [:set-active-panel :marketing-panel]))


(defroute "/docs" []
  (re-frame/dispatch [:set-active-panel :doc-panel]))


(defroute "/docs/:item" [item]
  #_(cond
    (= (keyword item) :inputs) (re-frame/dispatch-sync [:initialize-inputs])
    (= (keyword item) :sheet)  (re-frame/dispatch-sync [:init-sheet]))
  (re-frame/dispatch [:set-active-doc-item (keyword item)]))


;; TODO Figure out why the bottom route is always executed
#_(defroute "*" []
  (re-frame/dispatch [:set-active-panel :not-found]))


(defn init []
  (re-frame/dispatch-sync [:initialize-db])
  (re-frame/clear-subscription-cache!)
  (secretary/set-config! :prefix "#")
  (accountant/configure-navigation!
   {:nav-handler  (fn [path] (secretary/dispatch! path))
    :path-exists? (fn [path] (secretary/locate-route path))}))


(def dispatch accountant/dispatch-current!)
