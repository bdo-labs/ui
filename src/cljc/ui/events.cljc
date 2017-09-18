(ns ui.events
  (:require #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [re-frame.core :as re-frame]
            [ui.db :as db]
            [ui.util :as u]
            [ui.element.numbers.events]))


(defn- check-and-throw
  "Check specification against the current database"
  [a-spec db]
  (when-not (spec/valid? a-spec db)
    (throw (ex-info
            (str "database failed specification-check: "
                 (spec/explain-str a-spec db)) {}))))


(def ^{:private true}
  check-spec
  (re-frame/after (partial check-and-throw :ui.db/db)))


(def ^{:private true
       :doc "This is an interceptor-pipeline that every handler must run
             through for validation against the application-state."}
  interceptors
  [check-spec
   re-frame/trim-v])


(re-frame/reg-event-db :initialize-db (fn  [_ _] db/default-db))


(re-frame/reg-event-db
 :set-active-panel
 [interceptors]
 (fn [db [panel]]
   (assoc db :active-panel panel)))


(re-frame/reg-event-fx
 :set-active-doc-item
 [interceptors]
 (fn [{:keys [db]} [doc-item]]
   {:dispatch-n (list (when (= doc-item :sheet) [:init-sheet])
                      (when (= doc-item :icons) [:init-icons])
                      (when (= doc-item :inputs) [:init-inputs])
                      [:set-active-panel :doc-panel])
    :db (assoc db :active-doc-item doc-item)}))


(re-frame/reg-event-db
 :set-progress
 [interceptors]
 (fn [db [progress]]
   (assoc db :progress progress)))


(re-frame/reg-event-fx
 :navigate
 [interceptors]
 (fn [{:keys [db]} frag]
   (let [fragments (case (first frag)
                     :add  (flatten (conj (get db :fragments) (rest frag)))
                     :back (pop (vec (get db :fragments)))
                     frag)]
     {:navigate-to fragments
      :db          (assoc db :fragments fragments)})))

