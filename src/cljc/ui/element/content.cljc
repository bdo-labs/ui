(ns ui.element.content
  (:require [ui.util :as u]
            [clojure.string :as str]
            [clojure.spec :as spec]))


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

(defn article
  [& args]
  (let [{:keys [params content]} (u/conform-or-fail ::article args)]
    (into [:article (merge {:role :article} params)]
          (->> (map last content)
               (map section)))))


(defn vr [] [:div.Vertical-rule])


(defn hr [] [:div.Horizontal-rule])
