(ns ui.element.content.views
  (:require [ui.util :as util]
            [clojure.string :as str]
            [ui.element.containers.views :refer [container]]
            [ui.element.content.spec :as spec]))

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
  (let [{:keys [params content]} (util/conform! ::spec/article args)]
    [container {:rounded?   true
                :raised?    true
                :background :white
                :style {:margin "2em"}}
     (into [:article (merge {:role :article} params)]
           (map section content))]))

(defn vr [] [:div.Vertical-rule])

(defn hr [] [:div.Horizontal-rule])
