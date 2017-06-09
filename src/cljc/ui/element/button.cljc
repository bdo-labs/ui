(ns ui.element.button
  (:require #_[clojure.test.check.generators :as gen]
            [clojure.spec :as spec]
            [clojure.string :as str]
            [clojure.string :as str]
            [ui.util :as u]))


(spec/def ::flat? boolean?)


(spec/def ::rounded? boolean?)


(spec/def ::button-params
  (spec/keys
   :opt-un [::flat? ::rounded?]))


(spec/def ::content
  (spec/or :string string?
           :vector vector?))


(spec/def ::button-args
  (spec/cat
   :params ::button-params
   :content ::content))


(defn button
  ([content]
   [button {} content])
  ([{:keys [fill? flat? rounded? class] :as params} & content]
   (let [classes (u/names->str (concat [(when flat? :Flat)
                                        (when fill? :Fill)
                                        (when rounded? :Rounded)] class))]
     (into [:button.Button
            (merge (dissoc params
                           :fill?
                           :flat?
                           :rounded?) {:class classes})]
           content))))


(spec/fdef button
        :args ::button-args
        :ret vector?)
