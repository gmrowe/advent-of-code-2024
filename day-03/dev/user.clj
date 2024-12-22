(ns user
  (:require
   [clojure.tools.namespace.repl :refer [refresh]]
   [clojure.repl :refer [doc source] ]
   [kaocha.repl :as k]
   [day-03.main :as main]))

(defn test-all [] (refresh) (k/run :unit))
