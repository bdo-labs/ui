(ns ui.element.content
  (:require [ui.util :as u]
            [clojure.string :as str]))


(defn markdown
  "Render markdown-[text] as html"
  [text]
  (let [formatted (->> (str text)
                       (str/split-lines)
                       (map str/triml)
                       (map #(if (= "" %) "  " %))
                       (str/join "\n"))]
    [:section.Markdown
     {:dangerouslySetInnerHTML {:__html (u/md->html formatted)}}]))


(defn section
  [content]
  (cond
    (string? content) (markdown content)
    :else             [:section.Custom content]))


;; TODO Add parameters
(defn article
  ([& content]
   (into [:article {}] (map section content))))


(defn vr [] [:div.Vertical-rule])


(defn hr [] [:div.Horizontal-rule])
