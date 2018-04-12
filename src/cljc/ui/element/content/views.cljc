(ns ui.element.content.views
  (:require #?(:cljs [reagent.core :as reagent])
            [ui.util :as util]
            [clojure.string :as str]
            [ui.element.containers.views :refer [container]]
            [ui.element.content.spec :as spec]))

;; Views ------------------------------------------------------------------


#?(:cljs (defn highlight-code [html-node]
           (let [nodes (.querySelectorAll html-node "pre code")]
             (loop [i (.-length nodes)]
               (when-not (neg? i)
                 (when-let [item (.item nodes i)]
                   (.highlightBlock js/hljs item))
                 (recur (dec i)))))))

(defn markdown
  "Render markdown-[text] as html"
  [text]
  (let [formatted (->> (str text)
                       (str/split-lines)
                       ;; (map str/triml)
                       (str/join "\n")
                       (util/md->html))]
    #?(:clj [:section.Markdown formatted]
       :cljs
       (reagent/create-class
        {:display-name "markdown"
         :component-did-mount #(-> % (reagent/dom-node) (highlight-code))
         :reagent-render
         (fn [] [:section.Markdown
                 {:dangerouslySetInnerHTML {:__html formatted}}])}))))

(defn section
  [content]
  (cond
    (string? content) [markdown content]
    :else             [:section.Custom content]))

(defn article
  [& args]
  (let [{:keys [params content]} (util/conform! ::spec/article args)]
    [container {:background :white
                :scrollable? true}
     (into [:article (merge {:role :article} params)]
           (map section content))]))

(defn vr [] [:div.Vertical-rule])

(defn hr [] [:div.Horizontal-rule])
