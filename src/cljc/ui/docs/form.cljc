(ns ui.docs.form
  (:require [clojure.spec.alpha :as spec]
            [phrase.alpha :as phrase]
            [re-frame.core :as re-frame]
            [ui.elements :as element]
            [ui.layout :as layout]
            [ui.wire.polyglot :refer [translate]]
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
  {:on-valid (fn [data _] #?(:cljs (js/alert (pr-str data))))}
  [{:type ::element/numberfield
    :name :number1
    :label "Number (1)"
    :help "My help text"
    :text [:span {:style {:font-weight "bold"}} "My info text"]
    :wiring [:tr :$key
             [:td :$label]
             [:tr :$field
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
    :items [{:id 1 :value "Test 1"}
            {:id 2 :value "Test 2"}]}
   {:type ::element/checkbox
    :name :checkbox1
    :label "Checkbox"}])

(defn- notification-args [model]
  {:model model
   :class ["Error"]
   :notification {:class ["Error"]}})


(defn documentation[]
  (let [form-table          (testform {} {})
        form-list           (testform {} {})
        form-paragraph      (testform {} {})
        form-template       (testform {} {})
        form-wire           (testform {} {})
        table-error-sub     (re-frame/subscribe [::form/error (:id form-table) :number2])
        list-error-sub      (re-frame/subscribe [::form/error (:id form-list) :number2])
        paragraph-error-sub (re-frame/subscribe [::form/error (:id form-paragraph) :number2])
        template-error-sub  (re-frame/subscribe [::form/error (:id form-template) :number2])
        wire-error-sub      (re-frame/subscribe [::form/error (:id form-wire) :number2])]
    (fn []
      [layout/horizontally
       [layout/vertically
        [element/article
         "# We generate one form per type of rendering using the same form (testform)

## as-table"
         [form/as-table {} form-table]
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
                                       :$help
                                       ]} form-template]

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
       [layout/vertically
        [element/article
         "# Error notifications for the second input on all the forms"

         "### as-table"
         [element/notifications (notification-args table-error-sub)]

         "### as-list"
         [element/notifications (notification-args list-error-sub)]

         "### as-paragraph"
         [element/notifications (notification-args paragraph-error-sub)]

         "### as-template"
         [element/notifications (notification-args template-error-sub)]

         "### as-wire"
         [element/notifications (notification-args wire-error-sub)]]]])))
