(ns ui.util
  (:require [clojure.string :as str]
            #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [markdown.core :as markdown]
            [tongue.core :as tongue]
            [garden.color :as color]
            #?(:cljs [cljs.core :refer [random-uuid]])))



(defn extract
  "Extracts [key] from [db]"
  [db [key]]
  (-> db key))


(defn extract-or-false
  "Extracts [key] from [db] or return false if it doesn't exist"
  [db [key]]
  (or (-> db key) false))


(defn toggle
  "Toggle a boolean value found using the [k]ey from the [db]. Note
  that it will remove \"toggle-\" from the key"
  [db [k]]
  (let [k (keyword (subs (str/replace (str k) #"toggle-" "") 1))]
    (update-in db [k] not)))


(defn =i
  "Case-Insensitive string comparison"
  [& strs]
  (apply = (map #(str/upper-case %) strs)))


(defn xor
  "Explicit OR"
  [p q]
  (and (or p q)
       (not (and p q))))


(defn gen-id
  "Create a unique-id"
  []
  #?(:clj (str (java.util.UUID/randomUUID))
     :cljs (random-uuid)))


(defn log
  "Log all inputs to console or REPL, the input is returned for further manipulation"
  [& in]
  (do
    (apply #?(:clj println :cljs js/console.log) in)
    in))


(defn exception
  "Throw exceptions independently of environment"
  [error-string]
  (throw (#?(:clj Exception. :cljs js/Error.) error-string)))


(defn conform-or-fail
  "Conform arguments to specification or throw an exception"
  [spec args]
  (if (spec/valid? spec args)
    (spec/conform spec args)
    (exception (spec/explain-str spec args))))


(defn slug
  "Removes characters that are not URL-compliant"
  [& s]
  (-> (str/join " " s)
      (str/lower-case)
      (str/replace #" " "-")
      (str/replace #"[^a-zA-Z-]" "")
      (str/replace #"\-{2,}" "-")
      (str/replace #"^-" "")
      (str/replace #"-$" "")))


(defn md->html
  "Convert regular markdown-formatted text to html"
  [text]
  (#?(:clj markdown/md-to-html :cljs markdown/md->html) text))


(defn names->str
  "Joins sequences of strings or keywords and capitalizes each of them"
  [lst]
  (->> (remove nil? lst)
       (map #(if (vector? %) (slug (names->str %)) %))
       (map name)
       (map str/capitalize)
       (str/join " ")))


(defn char-range
  [start end]
  (map char (range (int start) (int end))))


(defn parse-int [s]
  #?(:clj (Integer/parseInt s)
     :cljs (js/parseInt s)))


;; TODO Figure out why the lazy version fails when used within a reagent-component
(def col-refs
  [:A :B :C :D :E :F :G :H :I :J :L :M :N :O :P :Q :R :S :T :U :V :W :X :Y :Z]
  ;; (let [alpha    (u/char-range \A \Z)
  ;;       char-seq (fn [refs]
  ;;                  (let [fmt    (comp keyword str)
  ;;                        splice (fn [xs] (for [x xs y alpha] (fmt (name x) y)))]
  ;;                    (apply concat (iterate splice (map fmt refs)))))]
  ;;   (char-seq alpha))
  )


(defn col-num [ref]
  (let [col-ref (keyword (str/replace (str ref) #"[^A-Z]" ""))]
   (parse-int (.indexOf col-refs col-ref))))


(defn row-num [ref]
  (let [ref (if (keyword? ref) (name ref) (str ref))]
   (dec (parse-int (str/replace ref #"[^0-9]" "")))))


(defn col-ref [ref]
  (keyword (str/replace (name ref) #"[^A-Z]*" "")))


(def code->key
  {13 "enter"
   38 "up"
   40 "down"})


(def inst-strings-en
  { :weekdays-narrow ["S" "M" "T" "W" "T" "F" "S"]
    :weekdays-short  ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"]
    :weekdays-long   ["Sunday" "Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday"]
    :months-narrow   ["J" "F" "M" "A" "M" "J" "J" "A" "S" "O" "N" "D"]
    :months-short    ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"]
    :months-long     ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"]
    :dayperiods      ["AM" "PM"]
    :eras-short      ["BC" "AD"]
    :eras-long       ["Before Christ" "Anno Domini"] })


(def format-inst
   (tongue/inst-formatter "{day}. {month-short}, {year}" inst-strings-en))


(def format-number-en
  (tongue/number-formatter { :group ","
                             :decimal "." }))


(defn dark?
  "Is the [r g b]-color supplied a dark color?"
  [{:keys [r g b]}]
  (> (- 1 (/ (+ (* 0.299 r)
                (* 0.587 g)
                (* 0.114 b)) 255)) 0.5))


(defn gray
  "Creates a [shade] of gray"
  [shade]
  (color/rgb (vec (take 3 (repeat shade)))))
