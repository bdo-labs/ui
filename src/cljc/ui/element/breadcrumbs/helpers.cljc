(ns ui.element.breadcrumbs.helpers
  (:require [clojure.string :as str]))

;; all functions are taken from github.com/emil0r/ez-web with permission from the author
;; some slight adaption has taken place

(defn- get-index [type crumb-data]
  (let [f (if (= type :even) even? odd?)]
    (filter f (range (* 2 (count crumb-data))))))

(defn- get-crumbs [{:keys [id crumb-data holder holder-attrib elem elem-attrib
                           href-attrib last? parts base-uri
                           separator separator-attrib]
                    :or {separator " » " last? true holder :ul elem :li
                         base-uri "" separator-attrib {:class "separator"}}}]
  [holder holder-attrib
   (interleave
    (map (fn [[index [uri name]]]
           ^{:key (str "crumb-" id "-" index)}
           [elem elem-attrib
            [:a (merge href-attrib {:href (str base-uri uri)})
             name]])
         (map vector (get-index :even crumb-data) crumb-data))
    (map (fn [[index _]]
           ^{:key (str "crumb-" id "-" index)}
           [elem separator-attrib separator])
         (map vector (get-index :odd crumb-data) (range (count crumb-data)))))
   (if last?
     [elem elem-attrib
      [:span (if (sequential? (last parts)) (second (last parts)) (last parts))]])])

(defn- crumb-string
  ([uri]
   (crumb-string uri {}))
  ([uri data]
   (let [parts (remove str/blank? (str/split uri #"/"))
         crumbs (reduce (fn [out index]
                          (if (= index (count parts))
                            out
                            (let [crumb (take (+ index 1) parts)]
                              (conj out [(str "/" (str/join "/" crumb))
                                         (last crumb)]))))
                        []
                        (range (count parts)))
         crumb-data (butlast crumbs)]
     {:crumb-data crumb-data
      :crumbs (get-crumbs (merge data {:crumb-data crumb-data :parts parts}))
      :last (last parts)})))

(defn- crumb-vector
  ([uri-data]
   (crumb-vector uri-data {}))
  ([uri-data data]
   (let [parts (remove (fn [[part _]] (str/blank? part)) uri-data)
         crumbs (reduce (fn [out index]
                          (if (= index (count parts))
                            out
                            (let [crumb (take (+ index 1) parts)]
                              (conj out [(str "/" (str/join "/" (map first crumb)))
                                         (second (nth crumb index))]))))
                        []
                        (range (count parts)))
         crumb-data (butlast crumbs)]
     {:crumb-data crumb-data
      :crumbs (get-crumbs (merge data {:crumb-data crumb-data :parts parts}))
      :last (last parts)})))

(defn- crumb-iseq
  ([uri-data]
   (crumb-vector (vec uri-data) {}))
  ([uri-data data]
   (crumb-vector (vec uri-data) data)))

(defmulti crumb
  "Takes a URI or a sequence of vectors with [URI part, name of URI]"
  (fn [to-crumb & _] (type to-crumb)))

#?(:clj  (defmethod crumb java.lang.String [& args] (apply crumb-string args)))
#?(:cljs (defmethod crumb js/String [& args] (apply crumb-string args)))

#?(:clj  (defmethod crumb clojure.lang.IPersistentVector [& args] (apply crumb-vector args)))
#?(:cljs (defmethod crumb cljs.core/PersistentVector [& args] (apply crumb-vector args)))

#?(:clj  (defmethod crumb clojure.lang.ISeq [& args] (apply crumb-iseq args)))
#?(:cljs (defmethod crumb cljs.core/ISeq [& args] (apply crumb-iseq args)))

(defmethod crumb :default [_ _])
