(ns ui.docs.form
  (:require [clojure.spec.alpha :as spec]
            [phrase.alpha :as phrase]
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.wire.polyglot :as polyglot :refer [translate]]
            [ui.wire.form :as form :refer [defform]]))

(phrase/defphraser #(> % min-number)
  [_ _ min-number]
  (translate :validation/min-number min-number))
(phrase/defphraser #(< % max-number)
  [_ _ max-number]
  (translate :validation/max-number max-number))
(phrase/defphraser number?
  [_ {:keys [val]}]
  (let [val-str (if (nil? val)
                  "nil"
                  (str val))]
    (translate :validation/NaN val-str)))

(spec/def ::number1-valid (spec/and number? #(> % 20) #(< % 50)))
(spec/def ::number2-valid (spec/and number? #(< % 30)))
(spec/def ::number-1+2 (spec/and ::number1-valid ::number2-valid))

(defform testform
  {:on-valid :dispatch}
  [{:type ::element/numberfield
    :name :number1
    :label "Number (1)"
    :help "My help text"
    :text [:span {:style {:font-weight "bold"}} "My info text"]
    :wiring [:tr :$key
             [:td :$label]
             [:td :$field
              :$errors
              [:div "We put this extra struff in"]
              :$help
              "And we removed the info text"]]
    :spec ::number1-valid}
   {:type element/numberfield
    :name :number2
    :label "Number (2)"
    :spec ::number2-valid
    :error-element :dispatch}
   {:type ::element/textfield
    :name :text1
    :label "Text"}
   {:type ::element/chooser
    :name :chooser1
    :label "Chooser"
    :deletable true
    :items #{{:id 1 :value "Test 1"}
             {:id 2 :value "Test 2"}}}
   {:type ::element/checkbox
    :name :checkbox1
    :label "Checkbox"}])

(defn- notification-args [model]
  {:model model
   :class ["Error"]
   :notification {:class ["Error"]}})

(defn alert-result [data]
  #?(:cljs (if (form/valid? data) (js/alert (pr-str data)))))


(defn documentation[]
  (let [form-table          (testform {} {})
        form-list           (testform {:on-valid alert-result} {})
        form-paragraph      (testform {:on-valid alert-result} {})
        form-template       (testform {} {})
        form-wire           (testform {:on-valid alert-result} {})
        form-wizard         (testform {:render :wizard
                                       :wizard {:valid-fn alert-result
                                                :steps [{:fields [:number1 :number2]
                                                         :legend [:h3 "Numbers"]}
                                                        {:fields [:text1 :chooser1]
                                                         :legend [:h3 "Text + Chooser"]}
                                                        {:fields [:checkbox1]
                                                         :legend [:h3 "Checkbox"]}]}}
                                      {})
        table-error-sub     (re-frame/subscribe [::form/error (:id form-table) :number2])
        list-error-sub      (re-frame/subscribe [::form/error (:id form-list) :number2])
        paragraph-error-sub (re-frame/subscribe [::form/error (:id form-paragraph) :number2])
        template-error-sub  (re-frame/subscribe [::form/error (:id form-template) :number2])
        wire-error-sub      (re-frame/subscribe [::form/error (:id form-wire) :number2])
        form-on-valid       (re-frame/subscribe [::form/on-valid (:id form-table)])
        wizard-current-step (re-frame/subscribe [::form/wizard-current-step (:id form-wizard)])
        form-button-send    (fn []
                              [:tr [:td] [:td [element/button {:class "primary"
                                                               :disabled (not (form/valid? @form-on-valid))} "Send"]]])]
    (fn []
      (let [base-params {:style {:padding "0"}}]
        [layout/vertically (merge base-params {:background :white
                                               :gap? false})
         [:a {:href "https://github.com/bdo-labs/ui/issues/new"
              :target :_blank}
          [element/icon {:title (polyglot/translate :ui/report-issue)
                         :style {:position :absolute
                                 :top      "1em"
                                 :right    "1em"}} "bug"]]

         [layout/horizontally (merge base-params {:gap? false
                                                  :compact? true
                                                  :fill? true})


          [layout/vertically (merge base-params
                                    {:compact? true :gap? false :fill? true}
                                    {:style {:flex-grow 2}})
           [element/article (merge base-params {:style {:width "100%" :padding "0"}})

            "# Form

```clojure
(ns your.namespace
  (:require [clojure.spec.alpha :as spec]
            [phrase.alpha :as phrase]
            [re-frame.core :as re-frame]
            [ui.wire.form :as form :refer [defform]]
            [ui.wire.polyglot :as polyglot :refer [translate]])


;; give back translations for the specs that can go wrong
;; see github.com/alexanderkiel/phrase for details
(phrase/defphraser #(> % min-number)
  [_ _ min-number]
  (translate :validation/min-number min-number))
(phrase/defphraser #(< % max-number)
  [_ _ max-number]
  (translate :validation/max-number max-number))
(phrase/defphraser number?
  [_ {:keys [val]}]
  (let [val-str (if (nil? val)
                  \"nil\"
                  (str val))]
    (translate :validation/NaN val-str)))

;; define specs to validate against
(spec/def ::number1-valid (spec/and number? #(> % 20) #(< % 50)))
(spec/def ::number2-valid (spec/and number? #(< % 30)))
(spec/def ::number-1+2 (spec/and ::number1-valid ::number2-valid))

(defn alert-result [data]
  (if (form/valid? data) (js/alert (pr-str data))))


(defform testform
  ;; :on-valid can be either a funtion
  ;; (fn [data] (comment \"do something\")) or :dispatch (default)
  ;; if the data is invalid the data sent in will be
  ;; :ui.wire.form/invalid, otherwise the data from the fields
  {:on-valid :dispatch
   ;; render as :wizard or :normal (default)
   ;; supports all render functions except for as-wire
   :render :wizard
   ;; :wizard is only if :render is set to :wizard
   :wizard {;; when the form is valid, which function should be called?
            ;; takes the data as input
            :valid-fn alert-result
            ;; define the steps of the wizard.
            ;; optional :legend
            :steps [{:fields [:number1 :number2]
                     :legend [:h3 \"Numbers\"]}
                    {:fields [:text1 :chooser1]
                              :legend [:h3 \"Text + Chooser\"]}
                    {:fields [:checkbox1]
                     :legend [:h3 \"Checkbox\"]
                     :style {:min-height \"10em\"}}]
            ;; define a style map for the wizard
            ;; if nothing is given a default map is used
            ;; which is 10em * max number of elements of all steps
            ;; the style map is looked for in descending order
            ;; 1) initial defform
            ;; 2) initilization of the form
            ;; 3) the rendering arguments for the form function (as-table, etc)
            ;; 4) each individual step
            :style {:min-height \"20em\"}}}
   }

  ;; define the fields
  ;; the types listed is what currently is supported
  [{;; :type can either be a namespaced ui.elements keyword
    ;; or a function
    :type ::element/numberfield
    ;; the name of the field, this is what will show up
    ;; in the data map for example
    :name :number1
    ;; label. used by the different rendering methods
    :label \"Number (1)\"
    ;; help text for the field, will show up beneath
    :help \"My help text\"
    ;; general text field, will show up beneath
    :text [:span {:style {:font-weight \"bold\"}} \"My info text\"]
    ;; override how a piece of is rendered
    :wiring [:tr :$key
             [:td :$label]
             [:td :$field
              :$errors
              [:div \"We put this extra struff in\"]
              :$help
              \"And we removed the info text\"]]
    ;; the spec to validate against
    :spec ::number1-valid}
   {:type element/numberfield
    :name :number2
    :label \"Number (2)\"
    :spec ::number2-valid
    ;; tell the renderer to not render the error
    ;; element, it will instead be handled elsewhere
    ;; through a re-frame subscription
    :error-element :dispatch}
   {:type ::element/textfield
    :name :text1
    :label \"Text\"}
   {:type ::element/chooser
    :name :chooser1
    :label \"Chooser\"
    :deletable true
    :items #{{:id 1 :value \"Test 1\"}
             {:id 2 :value \"Test 2\"}}}
   {:type ::element/checkbox
    :name :checkbox1
    :label \"Checkbox\"}])

;; first arg is options, second arg is data for the fields
(def myform (testform {} {}))

;; current subscriptions are supported by form
(re-frame/subscribe [:ui.wire.form/error (:id myform) :number1])
(re-frame/subscribe [:ui.wire.form/on-valid (:id myform)])
(re-frame/subscribe [:ui.wire.form/wizard-current-step (:id myform)])

;; supported render functions
;; the optional rendering is optional
[form/as-table {} myform [:div \"Optional rendering\"]]

[form/as-list {} myform [:div \"Optional rendering\"]]

[form/as-paragraph {} myform [:div \"Optional rendering\"]]

;; each field is rendered as in the template
;; supports :$<label|field|errors|help|text|key>
[form/as-template {:wiring {:number nil}
                   :template [:div.template :$key
                                          :$label
                                          :$field
                                          :$errors
                                          :$text
                                          :$help]} myform]

;; as-wire takes a :wiring option which holds a hiccup template
;; :$<field-name>.<label|field|errors|help|text|key> is supported
;; the optional rendering is handled directly by wire through
;; the :$content field
[form/as-wire {:wiring [:div.wire
                         [:div.number1
                           :$number1.label
                           :$number1.field
                           :$number1.errors]
                         [:div.number2
                           :$number2.label
                           :$number2.field
                           :$number2.errors]]} myform]

```
"

            "## as-table with :render set to :wizard"
            [:h4 "Current step (a re-frame subscription) which is handled outside the rendering by the wizard -> " [:span {:style {:background-color "teal"
                                                                   :color :white
                                                                   :padding "5px 8px"
                                                                   :border-radius "3px"}}
                                                    @wizard-current-step]]
            [form/as-table {} form-wizard]

            "## We generate one form per type of rendering using the same form (testform)

## as-table"
            [form/as-table {} form-table (form/table-button form-table alert-result)]
            "#### Second error output, using re-frame subscription (the rest will show up on the right column)"
            [element/notifications (notification-args table-error-sub)]


            "## as-list"
            [form/as-list {:wiring {:number1 nil}} form-list]

            "## as-paragraph"
            [form/as-paragraph {:wiring {:number1 nil}} form-paragraph]

            "## as-template"
            [form/as-template {:wiring {:number1 nil}
                               :template [:div.template :$key
                                          :$label
                                          :$field
                                          :$errors
                                          :$text
                                          :$help]} form-template]

            "## as-wire"
            [form/as-wire {:wiring
                           [:div.wire
                            [:div.number1
                             :$number1.label
                             :$number1.field
                             :$number1.errors]
                            [:div.number2
                             :$number2.label
                             :$number2.field
                             :$number2.errors]]} form-wire]]]

          [layout/vertically (merge base-params {:compact? true :gap? false :fill? true})
           [element/article {:style {:padding "0" :position :fixed}}
            "## @notifications"

            "### as-table"
            [element/notifications (notification-args table-error-sub)]

            "### as-list"
            [element/notifications (notification-args list-error-sub)]

            "### as-paragraph"
            [element/notifications (notification-args paragraph-error-sub)]

            "### as-template"
            [element/notifications (notification-args template-error-sub)]

            "### as-wire"
            [element/notifications (notification-args wire-error-sub)]]]]]))))
