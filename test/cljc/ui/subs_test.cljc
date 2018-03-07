(ns ui.subs-test
  (:require [clojure.test :refer [deftest testing is]]
            [ui.subs :as subs]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 1))))
