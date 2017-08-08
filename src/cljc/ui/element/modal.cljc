(ns ui.element.modal
  #?(:cljs (:require-macros [garden.def :refer [defcssfn]]))
  (:require #?(:clj [garden.def :refer [defcssfn]])
            [ui.element.containers :refer [container]]
            [ui.element.button :refer [button]]
            [ui.element.icon :refer [icon]]
            [clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [garden.units :as unit]
            [garden.color :as color]
            [ui.util :as u]))


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
                  :z-index   101}]]
    [:.Content {:position  :absolute
                :left      (unit/percent 50)
                :top       (unit/percent 50)
                :transform [[(translateY (unit/percent -50)) (translateX (unit/percent -50))]]
                :z-index   102}
     [:.Container {:animation [[:fade-up :200ms :ease]]
                   :padding   [[(unit/rem 2.5) (unit/rem 5)]]
                   :position  :relative
                   :z-index   103}]]]
   [:.Close {:position :absolute
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


#_(spec/def ::open? boolean?)
#_(spec/def ::confirm fn?)
#_(spec/def ::cancel-on-backdrop? boolean?)
#_(spec/def ::confirm-text (spec/and string? not-empty))
#_(spec/def ::cancel-text (spec/and string? not-empty))
#_(spec/def ::dialog-params
  (spec/keys :opt-un [::open?
                   ::confirm
                   ::cancel-on-backdrop?
                   ::confirm-text
                   ::cancel-text]))
#_(spec/def ::dialog-body
  (spec/or :string string?
        :formatted vector?))


(spec/def ::stub
  (spec/with-gen fn?
    (gen/return (constantly nil))))


(spec/def ::show? boolean?)
(spec/def ::backdrop? boolean?)
(spec/def ::close-button? boolean?)
(spec/def ::cancel-on-backdrop? boolean?)
(spec/def ::hide ::stub)


(spec/def ::params
  (spec/keys :opt-un [::show?
                      ::close-button?
                      ::backdrop?
                      ::cancel-on-backdrop?]))


(spec/def ::content (spec/* (spec/or :str string? :vec vector?)))


(spec/def ::args
  (spec/cat :params ::params
            :content ::content))


;; TODO Improve animation
(defn dialog [& args]
  (let [{:keys [params content]}           (u/conform-or-fail ::args args)
        {:keys [show? backdrop? cancel-on-backdrop? close-button? cancel]
         :or   {backdrop?           true
                cancel-on-backdrop? true
                close-button?       true}} params
        class                              (u/params->classes params)
        ui-params                          (conj (u/keys-from-spec ::params) :class)
        params                             (->> (apply dissoc params ui-params)
                                                (merge {:class class}))]
    (when show? [:div.Dialog params
                 [:div.Content
                  (when close-button?
                    [icon {:font     "ion"
                           :size     4
                           :class    "Close"
                           :on-click cancel} "ios-close-empty"])
                  (into [container {:raised?  true
                                    :rounded? true
                                    :inline?  true
                                    :layout   :vertically
                                    :style    {:background :white}}]
                        (mapv last content))]
                 (when backdrop?
                   [:div.Backdrop (when cancel-on-backdrop? {:on-click cancel})])])))


(spec/def ::confirm-label string?)
(spec/def ::cancel-label string?)
(spec/def ::on-confirm ::stub)
(spec/def ::on-cancel ::stub)


(spec/def ::confirm-params
  (spec/keys :opt-un [::confirm-label
                      ::cancel-label
                      ::on-cancel]
             :req-un [::on-confirm]))


(spec/def ::confirm-args
  (spec/cat :params ::confirm-params
            :content ::content))


(defn confirm-dialog [& args]
  (let [{:keys [params content]} (u/conform-or-fail ::confirm-args args)
        {:keys [on-cancel on-confirm cancel-label confirm-label]
         :or {cancel-label "No"
              confirm-label "Yes"
              on-cancel #(u/log "Cancel")}} params
        ui-params (u/keys-from-spec ::confirm-params)
        params (apply dissoc params ui-params)]
    [:div
     [:pre (pr-str params)]
     [dialog params
      [container {:layout :vertically}
       [:p (map last content)]
       [container {:layout :horizontally}
        [button {:on-click on-confirm} confirm-label]
        [button {:on-click on-cancel} cancel-label]]]]]))


(defn alert-dialog [& args]
  (let [{:keys [params content]} (u/conform-or-fail ::alert-args args)
        {:keys [alert-label on-cancel]
         :or {alert-label "OK"}} params]
    [dialog params
     [:h3 (map last content)]
     [container {:layout :centered}
      [button {:on-click on-cancel} alert-label]]]))


#_(defn dialog
  [{:keys [confirm confirm-text cancel-text open cancel-on-backdrop?]
    :or   {confirm            false
           confirm-text       "Yes"
           cancel-text        "No"
           open               false
           cancel-on-backdrop? true}
    :as   params} content]
  (let [classes (u/names->str (concat [(when (true? open) :Open)]
                                      (:class params)))]
    [:div.Dialog {:class classes}
     [:div.Backdrop {:on-click #(confirm false)}]
     #_[container {:raised? true
                 :background "white"
                 :rounded? true
                 :layout :vertically}
      [:h3 "Dialog"]]]))

(spec/fdef dialog
        :args (spec/cat :params ::dialog-params
                     :body ::dialog-body)
        :ret vector?
        :fn dialog)

