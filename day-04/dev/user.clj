(ns user
  (:require
   [clojure.tools.namespace.repl :refer [refresh]]
   [kaocha.repl :as k]
   [day-04.main :as main]))

(defn test-all
 []
 (refresh)
 (k/run :unit))
