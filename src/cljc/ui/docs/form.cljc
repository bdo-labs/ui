(ns ui.docs.form
  (:require [clojure.spec.alpha :as spec]
            [ui.elements :as element]
            [ui.element.form :as form :refer [defform]]
            [ui.layout :as layout]))


(spec/def ::number1-valid #(> % 30))

(defform testform
  {:on-valid (fn [data _] #?(:cljs (js/alert (pr-str data))))}
  [{:type ::element/numberfield
    :name :number1
    :spec ::number1-valid}
   {:type element/numberfield
    :name :number2}])

(defn documentation[]
  (let [f (testform {} {})]
    (fn []
     [element/article
      "# Hi there"
      [layout/vertically
       [form/as-table {:on-valid :test} f]]])))

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
