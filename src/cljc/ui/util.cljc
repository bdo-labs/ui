(ns ui.util
  (:require #?(:cljs [cljs.core :refer [random-uuid]])
            [clojure.string :as str]
            [clojure.spec.alpha :as spec]
            [markdown.core :as markdown]
            [tongue.core :as tongue]
            [garden.color :as color]
            [ui.spec-helper :as h]))


(defn log
  "Log all inputs to console or REPL, the input is returned for further manipulation"
  [& in]
  (do
    (apply #?(:clj println :cljs js/console.log) in)
    in))


(defn time-of [cycles f]
  (->> f
       (dotimes [_ cycles])
       (time)
       (with-out-str)))


(defn ->ref-name [event]
  (let [datom (str/replace (name event) #"^(toggle|set|add|remove)-" "")]
    (keyword (str (namespace event) "/" datom))))



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
  (apply = (mapv str/lower-case strs)))


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


(defn exception
  "Throw exceptions independently of environment"
  [error-string]
  (throw (#?(:clj Exception. :cljs js/Error.) error-string)))


(defn conform-or-fail
  "Conform arguments to specification or throw an exception"
  [spec args]
  (spec/conform spec args)
  #_(if (spec/valid? spec args)
    (spec/conform spec args)
    (exception (spec/explain-str spec args))))


(def ^:private +slug-tr-map+
  (zipmap "ąàáäâãåæăćčĉęèéëêĝĥìíïîĵłľńňòóöőôõðøśșšŝťțŭùúüűûñÿýçżźž"
          "aaaaaaaaaccceeeeeghiiiijllnnoooooooossssttuuuuuunyyczzz"))

(defn lower
  "Converts string to all lower-case.
  This function works in strictly locale independent way,
  if you want a localized version, just use `locale-lower`"
  [s]
  (when (string? s)
    (.toLowerCase #?(:clj ^String s :cljs s))))


(defn slug
  "Transform text into a URL slug"
  [& s]
  (some-> (lower (str/join " " (flatten s)))
          (str/escape +slug-tr-map+)
          (str/replace #"[^\w\s]+" "")
          (str/replace #"\s+" "-")))


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
       ;; (map str/capitalize)
       (str/join " ")
       (str/lower-case)))


(defn char-range
  [start end]
  (map char (range (int start) (int end))))


(defn parse-int [s]
  #?(:clj (Integer/parseInt s)
     :cljs (js/parseInt s)))


;; TODO Figure out why the lazy version fails when used within a reagent-component
(def col-refs
  [:A :B :C :D :E :F :G :H :I :J :L :M :N :O :P :Q :R :S :T :U :V :W :X :Y :Z]
  ;; (let [alpha    (range \A \Z)
  ;;       char-seq (fn [refs]
  ;;                  (let [fmt    (comp keyword str)
  ;;                        splice (fn [xs] (for [x xs y alpha] (fmt (name x) y)))]
  ;;                    (apply concat (iterate splice (map fmt refs)))))]
  ;;   (char-seq alpha))
)


(defn col-num [cell-ref]
  (let [col-ref (keyword (str/replace (str cell-ref) #"[^A-Z]" ""))]
    (parse-int (.indexOf col-refs col-ref))))


(defn row-num [cell-ref]
  (let [cell-ref (if (keyword? cell-ref) (name cell-ref) (str cell-ref))]
    (parse-int (str/replace cell-ref #"[^0-9]" ""))))


(defn col-ref [cell-ref]
  (keyword (str/replace (name cell-ref) #"[^A-Z]*" "")))


(defn ref->cell-ref [ref]
  (let [cell (assoc ref :col (name (get col-refs (:col ref))))]
    (keyword (str/join (vals cell)))))


(defn cell-ref [ref f dir]
  (let [cell {:col (col-num ref)
              :row (inc (row-num ref))}]
    (ref->cell-ref (update cell dir f))))


;; Note that this is just a small sub-set of available keys
(def code->key
  (merge {8  "backspace"
          9  "tab"
          13 "enter"
          16 "shift"
          17 "ctrl"
          18 "alt"
          27 "esc"
          32 "space"
          37 "left"
          38 "up"
          39 "right"
          40 "down"
          46 "delete"
          91 "cmd"}
         ;; A - Z
         (let [alphacodes (range 65 (inc 90))]
           (zipmap alphacodes (map char alphacodes)))))


(def inst-strings-en
  {:weekdays-narrow ["S" "M" "T" "W" "T" "F" "S"]
   :weekdays-short  ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"]
   :weekdays-long   ["Sunday" "Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday"]
   :months-narrow   ["J" "F" "M" "A" "M" "J" "J" "A" "S" "O" "N" "D"]
   :months-short    ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"]
   :months-long     ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"]
   :dayperiods      ["AM" "PM"]
   :eras-short      ["BC" "AD"]
   :eras-long       ["Before Christ" "Anno Domini"]})


(def format-inst
  (tongue/inst-formatter "{day}. {month-short}, {year}" inst-strings-en))


(def format-number-en
  (tongue/number-formatter {:group ","
                            :decimal "."}))


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


(defn keys-from-spec [s]
  (->> (h/extract-spec-keys s)
       (filter vector?)
       (merge)
       (flatten)
       (mapv (comp keyword name))))


(defn param->class [[k v]]
  (-> (cond
        (vector? v)  (str (name k) "-" (str/join "-" (map name v)))
        (keyword? v) (str (name k) "-" (name v))
        (string? v)  (name k)
        (true? v)    (name k)
        :else        "")
      (str/replace #"\?" "")))


(defn params->classes [params]
  (->> params
       (keep param->class)
       (str/join " ")
       (str (:class params) " ")
       (str/trim)))


(defn aligned->align [v]
  (case v
    (:top :left)     :start
    (:bottom :right) :end
    :center))


(defn route [& fragments]
  (str/join "/" fragments))


(defn debounce
  [f delay]
  #?(:clj f
     :cljs (if (nat-int? delay)
             (let [timeout* (atom nil)]
               (fn [event]
                 (js/clearTimeout @timeout*)
                 (reset! timeout* (js/setTimeout (partial f event) delay))))
             f)))


(defn js->cljs [obj]
  #?(:clj obj
     :cljs
     (-> obj
         (js/JSON.stringify)
         (js/JSON.parse)
         (js->clj :keywordize-keys true))))


(defn smart-case-includes? [substr s]
  (if-not (empty? (re-find #"[A-Z]" substr))
    (str/includes? s substr)
    (when-not (empty? s)
     (str/includes? (str/lower-case s) (str/lower-case substr)))))

