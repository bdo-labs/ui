(ns ui.wire.wiring
  (:require [clojure.string :as str]
            [clojure.zip :as zip]
            [ui.zipper :refer [zipper]]))


(defn material [node]
  (if (and (keyword? node)
           (str/starts-with? (str node) ":$"))
    node))

(defn unwrapper [loc materials]
  (let [rest-of-material (zip/rights loc)]
    (-> loc
        (zip/up)
        (zip/replace rest-of-material))))

(defn wire [materials loc]
  (let [node (zip/node loc)
        mat (material node)]
    (println {:node node
              :mat mat})
    (if (contains? materials mat)
      (let [value (get materials mat)]
        (if (fn? value)
         (value loc materials)
         (zip/replace loc value)))
      loc)))


(defn wiring [frame materials]
  (loop [loc (zipper frame)]
    (let [next-loc (zip/next loc)]
      (if (zip/end? next-loc)
        (zip/root loc)
        (recur (wire materials next-loc))))))
