(ns ui.layout
  "
  Layout

  A set of functions that will work for 99% of your layout-needs.
  "
  (:require [ui.elements :as element]
            #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]))

(defn fill
  "# Fill

   ### Fill the void in whatever direction it's parent decides
  "
  [& content]
  (into [:span.Fill] content))

(spec/def ::centered-params (spec/cat))
(spec/def ::centered-content (spec/and vector? not-empty))
(spec/def ::centered-args
  (spec/cat :params ::centered-params
         :content ::centered-content))

;; TODO Introduce `:start-from` and `:space`
;;    | :space would be one of [:between :around :none], whereas :none is
;;    | the equivalent of align stretched
;;    | :start-from is context-driven, so if the layout is horizontal, you
;;    | can set it to [:left :right :center], whilst with vertical layouts
;;    | would have [:top :middle :bottom]

(defn centered
  "# Centered

   ### Layout children; centered both horizontally and vertically
  "
  [params & content]
  (let [local-props {:justify "center" :align "center"}]
    (if-not (map? params)
      [element/container local-props params
       (map-indexed #(with-meta %2 {:key (str "center-" %1)}) content)]
      [element/container (merge params local-props)
       (map-indexed #(with-meta %2 {:key (str "center-" %1)}) content)])))

(spec/fdef centered
        :args ::centered-args
        :ret vector?)

(defn horizontally
  "#Horizontally

   ### Layout children horizontally
  "
  [params & content]
  (let [local-props {:direction "row"}]
    (if-not (map? params)
      [element/container local-props params
       (map-indexed #(with-meta %2 {:key (str "horizontally-" %1)}) content)]
      (let []
       [element/container (merge params local-props)
        (map-indexed #(with-meta %2 {:key (str "horizontally-" %1)}) content)]))))

(defn vertically
  "#Vertically

   ### Layout children vertically
  "
  [params & content]
  (let [local-props {:direction "column"}]
    (if-not (map? params)
      [element/container local-props params
       (map-indexed #(with-meta %2 {:key (str "vertically-" %1)}) content)]
      [element/container (merge params local-props)
       (map-indexed #(with-meta %2 {:key (str "vertically-" %1)}) content)])))
