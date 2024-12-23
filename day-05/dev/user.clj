(ns user
  (:require
   [clojure.tools.namespace.repl :refer [refresh]]
   [day-05.main :as main]
   [kaocha.repl :as k]))

(defn test-all
 []
 (refresh)
 (k/run :unit))
