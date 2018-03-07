(ns ui.util-test
  (:require [ui.util :as util]
            [clojure.test :refer [deftest testing is]]))


(deftest param->class
  (testing "Classnames from keywords"
    (is (= "foo-bar" (util/param->class [:foo :bar]))
        (= "foo bar" (util/param->class [:foo [:bar]]))))

  (testing "Classnames from keywords where the value is true"
    (is (= "visible" (util/param->class [:visible true])))
    (is (= "" (util/param-class [:hidden false])))))
