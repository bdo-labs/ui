(ns ui.element.modal
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [ui.element.containers :refer [container]]
            [ui.element.button :refer [button]]
            [ui.element.icon :refer [icon]]
            [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as spec]
            [garden.units :as unit]
            [garden.color :as color]
            [ui.util :as util]))


(defcssfn translateX)
(defcssfn translateY)


(defn style [{:keys [primary secondary background]}]
  [[:.Dialog {:position :fixed
              :left     0
              :top      0
              :height   (unit/percent 100)
              :width    (unit/percent 100)
              :margin   [[0 :!important]]
              :z-index  100}
    ;; [(selector/& (selector/not :.Open)) {:display :none}]
    [:&.show
     [:.Backdrop {:opacity   1
                  :animation [[:fade :200ms :ease]]
                  :z-index 99}]]
    [:.Content {:position  :absolute
                :left      (unit/percent 50)
                :top       (unit/percent 50)
                :transform [[(translateY (unit/percent -50)) (translateX (unit/percent -50))]]
                :z-index 101}
     [:.Container {:animation [[:fade-up :200ms :ease]]
                   :position  :relative}
      ;; TODO This hack should be corrected!
      [:&.raised {:overflow :visible}]]]]
   [:.Close {:position :absolute
             :cursor :pointer
             :top 0
             :right (unit/rem 1)
             :z-index 104}]

   [:body {:background-color background}]
   [:menu [:a {:display :block}]]
   [:a {:color           secondary
        :text-decoration :none}
    [:&.primary {:color primary}]
    [:&:hover {:color (color/darken secondary 30)}
     [:&.primary {:color primary}]]]])


(spec/def ::stub
  (spec/with-gen fn?
    (gen/return (constantly nil))))


(spec/def ::show? boolean?)
(spec/def ::backdrop? boolean?)
(spec/def ::close-button? boolean?)
(spec/def ::cancel-on-backdrop? boolean?)
(spec/def ::on-cancel ::stub)
(spec/def ::hide ::stub)


(spec/def ::params
  (spec/keys :req-un [::on-cancel]
             :opt-un [::show?
                      ::close-button?
                      ::backdrop?
                      ::cancel-on-backdrop?]))


(spec/def ::content (spec/* (spec/or :str string? :vec vector?)))


(spec/def ::args
  (spec/cat :params ::params
            :content ::content))


(defn dialog [& args]
  (let [{:keys [params content]}           (util/conform! ::args args)
        {:keys [show? backdrop? cancel-on-backdrop? close-button? on-cancel]
         :or   {backdrop?           true
                cancel-on-backdrop? true
                close-button?       true}} params
        class                              (util/params->classes params)
        ui-params                          (conj (util/keys-from-spec ::params) :class)
        params                             (->> (apply dissoc params ui-params)
                                                (merge {:class class}))]
    (when show? [:div.Dialog params
                 [:div.Content
                  (when close-button?
                    [icon {:font     "ion"
                           :size     3
                           :class    "Close"
                           :on-click on-cancel} "ios-close-empty"])
                  (into [container {:raised?  true
                                    :rounded? true
                                    :inline?  true
                                    :gap?     false
                                    :layout   :vertically
                                    :style    {:background :white}}]
                        (mapv last content))]
                 (when backdrop?
                   [:div.Backdrop (when cancel-on-backdrop? {:on-click on-cancel})])])))


(spec/def ::confirm-label string?)
(spec/def ::cancel-label string?)
(spec/def ::on-confirm ::stub)


(spec/def ::confirm-params
  (spec/keys :opt-un [::confirm-label
                      ::cancel-label]
             :req-un [::on-confirm
                      ::on-cancel]))


(spec/def ::confirm-args
  (spec/cat :params ::confirm-params
            :content ::content))


(defn confirm-dialog [& args]
  (let [{:keys [params content]} (util/conform! ::confirm-args args)
        {:keys [on-confirm on-cancel cancel-label confirm-label]
         :or {cancel-label "No"
              confirm-label "Yes"}} params
        ui-params (util/keys-from-spec ::confirm-params)
        params (assoc (apply dissoc params ui-params) :on-cancel on-cancel)]
    [dialog params
     [container {:layout :vertically}
      [:p (map last content)]
      [container {:layout :horizontally}
       [button {:on-click on-confirm} confirm-label]
       [button {:on-click on-cancel} cancel-label]]]]))


(defn alert-dialog [& args]
  (let [{:keys [params content]} (util/conform! ::alert-args args)
        {:keys [alert-label on-cancel]
         :or {alert-label "OK"}} params]
    [dialog params
     [:h3 (map last content)]
     [container {:layout :centered}
      [button {:on-click on-cancel} alert-label]]]))
