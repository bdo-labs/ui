(ns ui.element.content
  (:require [ui.util :as util]
            [clojure.string :as str]
            [clojure.spec.alpha :as spec]
            [ui.element.containers :refer [container]]))


;; Specification ----------------------------------------------------------


(spec/def ::class string?)


(spec/def ::article-params
  (spec/keys :opt-un [::class]))


(spec/def ::content
  (spec/or :str string?
           :vec vector?
           :nil nil?))


(spec/def ::article
  (spec/cat :params (spec/? ::article-params)
            :content (spec/* ::content)))


;; Views ------------------------------------------------------------------


(defn markdown
  "Render markdown-[text] as html"
  [text]
  (let [formatted (->> (str text)
                       (str/split-lines)
                       (map str/triml)
                       (map #(if (= "" %) "  " %))
                       (str/join "\n"))]
    [:section.Markdown
     {:dangerouslySetInnerHTML {:__html (util/md->html formatted)}}]))


(defn section
  [content]
  (cond
    (string? content) (markdown content)
    :else             [:section.Custom content]))


(defn article
  [& args]
  (let [{:keys [params content]} (util/conform! ::article args)]
    [container {:rounded?   true
                :raised?    true
                :background :white
                :style {:margin "2em"}}
     (into [:article (merge {:role :article} params)]
           (->> (map last content)
                (map section)))]))


(defn vr [] [:div.Vertical-rule])


(defn hr [] [:div.Horizontal-rule])
