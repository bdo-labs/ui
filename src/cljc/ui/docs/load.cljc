(ns ui.docs.load
  (:require [ui.elements :as element]
            [ui.layout :as layout]
            [ui.util :as util]))

(defn documentation []
  [element/article
   "## Load

   When managing workloads, you'll often want to give some feedback to  
   the end-user. `Load` can be used in symphony with  
   progress-indicators to do exactly that.  

   You will most typically use load from your fx-handlers. Ex:  
   ```
   (re-frame/reg-event-fx
     :process-user
     (fn [{:keys [db]} [user]]
       {:remove-load :user
        :db (assoc db :user user)}))


   (re-frame/reg-event-fx
     :user
     (fn [{:keys [db]} [k id]]
       {:get {:url \"/api/users/\" id
              :on-success process-user}
        :set-load [k]}))
   ```

   And then you can subscribe to that load. Ex:  
   ```
   @(re-frame/subscribe [:ui/load :user])
   ```

   It's worth noting that `set-load` can alternatively take a second  
   parameter, which would become the value of the load instead of the  
   default value of true. This is useful when you would like to report  
   a numeric progress-value or you would like to have a separate  
   action for when loading fails.  
   "])
