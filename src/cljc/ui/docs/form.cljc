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

(spec/explain-data ::number-1+2 3)

(defform testform
  {:on-valid (fn [data _] #?(:cljs (js/alert (pr-str data))))}
  [{:type ::element/numberfield
    :name :number1
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
    :spec ::number2-valid
    :error-element :dispatch}])

;;(testform {} {})

(defn documentation[]
  (let [form-table          (testform {} {})
        form-list           (testform {} {})
        form-paragraph      (testform {} {})
        table-error-sub     (re-frame/subscribe [::form/error (:id form-table) :number2])
        list-error-sub      (re-frame/subscribe [::form/error (:id form-list) :number2])
        paragraph-error-sub (re-frame/subscribe [::form/error (:id form-paragraph) :number2])]
    (fn []
      [element/article
       "# We generate one form per type of rendering using the same form (testform)

## as-table"
       [form/as-table {} form-table]
       "#### Second error output, using re-frame subscription"

       [element/notifications {:model table-error-sub}]


       "## as-list"
       [form/as-list {:wiring {:number1 nil}} form-list]
       "#### Second error output"

       [element/notifications {:model list-error-sub}]

       "## as-paragraph"
       [form/as-paragraph {:wiring {:number1 nil}} form-paragraph]
       "#### Second error output"

       [element/notifications {:model paragraph-error-sub}]])))

;; (comment

;;   (require '[ui.wire.form :as form])

;;   ;; defform is a macro for creating a function
;;   (defform testform
;;     ;; default opts, can be overridden when the form is called
;;     {:opts :here
;;      ;; accept function or keyword (to be dispatched)
;;      :on-valid (fn [opts values])
;;      :on-valid ::reframe-event-valid ;; [::reframe-event opts values]
;;      ;; same as :on-valid
;;      :on-invalid (fn [opts values])
;;      :on-invalid ::reframe-event-invalid

;;      ;; same as :on-valid
;;      :on-change (fn [opts vallue])
;;      :on-change ::reframe-event-on-change
;;      }
;;     [{:type element/numberfield
;;       :name "required"
;;       :label "Label"
;;       ;; this is taken from ez-form. at first I only had text, but found that text
;;       ;; specifically targeted for help was extremely useful. would like to find a
;;       ;; general purpuse way to include something like that
;;       :text "General info text"
;;       ;; hiccup can be sent in as well and will override any wrappers used
;;       :help [:div.help "Help text"]
;;       ;; if spec is provided, run the results against the spec and check for validity
;;       ;; if error-element is not turned off the error is shown
;;       :spec ::spec
;;       ;; default is element/error, automatically created inside the form with wiring setup
;;       ;; if a keyword is provided, the error message and the faulty spec will be dispatched to that keyword
;;       :error-element nil}

;;      ;; :type can be set to a :ui.elements/<element> keyword
;;      {:type ::element/numberfield
;;       :name ::test}])


;;   (let [f (testform {:data {"required" 1
;;                             ::test 2}
;;                      :on-valid ::is-valid})]
;;     [form/as-table {:class "table table-spaced"} f]
;;     [form/as-paragraph {:style {:margin-bottom "20px"}} f]
;;     [form/as-list {:type :ul} f]
;;     [form/as-template
;;      ;; for each element use the following snippet as a template
;;      ;; $label, $errors
;;      [:div
;;       [:span.label :$label]
;;       :$errors
;;       [:div.input :$element]]
;;      {:class "test"} f]
;;     [form/as-flow
;;      ;; a flowchart
;;      [:div.columns
;;       [:div.left
;;        :$required.label]
;;       [:div.right
;;        :$required.errors
;;        :$required.element
;;        [:p.text :$required.text]
;;        [:div.help :$required.help]]]
;;      ;; send in a nil as options
;;      nil
;;      ;; the form
;;      f])

;;   ;; thoughts on error element


;;   (element/error {;; model is either keyword for subscribing to subs,
;;                   ;; a ratom or a value
;;                   :model ::sub-for-errors
;;                   ;; need a number of options controlling behaviour
;;                   ;; such as
;;                   ;; 1) does the error always show, or does go away after a condition is met?
;;                   ;; 2) what conditions would there be? (timeout for example)
;;                   ;; 3) how does the error go away? fancy animations support?
;;                   ;; 4) controlling the conditions (e.g. timeout in milliseconds)
;;                   })


;;   )
