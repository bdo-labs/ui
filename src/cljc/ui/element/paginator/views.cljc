(ns ui.element.paginator.views
  (:require [clojure.string :as str]
            [ui.element.paginator.spec :as spec]
            [ui.wire.polyglot :refer [translate]]
            [ui.util :as util]))


;; paginate is taken from github.com/emil0r/ez-web with permission from the author
(defmulti paginate
  "Paginate the incoming collection/length"
  (fn [coll? _ _] (sequential? coll?)))
(defmethod paginate true [coll count-per-page page]
  (paginate (count coll) count-per-page page))
(defmethod paginate :default [length count-per-page page]
  (let [pages (+ (int (/ length count-per-page))
                 (if (zero? (mod length count-per-page))
                   0
                   1))
        page (if (and (string? page)
                      (not= page ""))
               #?(:clj  (Integer/parseInt page)
                  :cljs (js/parseInt page))
               page)
        page (cond
              (nil? page) 1
              (or (neg? page) (zero? page)) 1
              (> page pages) pages
              :else page)
        next (+ page 1)
        prev (- page 1)]
    (let [prev (if (or (neg? prev) (zero? prev)) nil prev)]
      {:pages pages
       :page page
       :next-seq (range (inc page) (inc pages))
       :prev-seq (reverse (range 1 (if (nil? prev) 1
                                       (inc prev))))
       :next (if (> next pages) nil next)
       :prev prev})))

(defn- render-page [on-change model page]
  (let [current? (= @model page)]
    [:li [:span {:class (if current? "active" "")
                 :on-click #(when-not current?
                              (reset! model page)
                              (when (ifn? on-change) (on-change page)))} page]]))

(defn- render-prev [on-change model page]
  (let [beginning? (= @model 1)]
    [:li.Prev
     [:span {:class (if beginning? "disabled" "")
             :on-click #(when-not beginning?
                          (swap! model dec)
                          (when (ifn? on-change) (on-change @model)))}
      (translate ::prev)]]))

(defn- render-next [on-change model page end-limiter]
  (let [end? (= @model end-limiter)]
    [:li.Next
     [:span {:class (if end? "disabled" "")
             :on-click #(when-not end?
                          (swap! model inc)
                          (when (ifn? on-change) (on-change @model)))}
      (translate ::next)]]))

(defn- get-end-limiter [length count-per-page]
  (int (if (zero? (mod length count-per-page))
         (/ length count-per-page)
         (inc (/ length count-per-page)))))


(defn paginator-
  [& args]
  (let [{:keys [params]} (util/conform! ::spec/args args)
        {:keys [id on-change model length edge count-per-page]
         :or   {id (util/gen-id)
                count-per-page 10
                edge 3}} params
        length (if (number? length) length (count length))
        end-limiter (get-end-limiter length count-per-page)]
    (fn [& args]
      (let [{:keys [params]} (util/conform! ::spec/args args)
            {:keys [style]
             :or   {style {}}} params
            {:keys [page
                    next-seq
                    prev-seq]} (paginate length count-per-page @model)
            -prev-seq (reverse (if edge (take edge prev-seq) prev-seq))
            -next-seq (if edge (take (max edge
                                          (+ edge (- edge (count prev-seq)))) next-seq) next-seq)
            ;; double check if we need to reset the boundary for prev-seq
            -prev-seq (if (and edge
                              (= (count -next-seq) edge))
                        -prev-seq
                        (reverse (take (+ edge edge (- (count -next-seq))) prev-seq)))]
        [:div.Paginator {:key (util/slug "paginator" id)
                         :style style}
         [:ul
          [render-prev on-change model page]
          (for [[index page] (map vector (range (count prev-seq)) -prev-seq)]
            ^{:key (str "paginator" id index)}
            [render-page on-change model page])
          [render-page on-change model page]
          (for [[index page] (map vector (range (count next-seq)) -next-seq)]
            ^{:key (str "paginator" id index)}
            [render-page on-change model page])
          [render-next on-change model page end-limiter]]]))))
