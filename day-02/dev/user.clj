(ns user
  (:require
   [clojure.tools.namespace.repl :refer [refresh]]
   [clojure.repl :refer [doc source]]
   [kaocha.repl :as k]))

(defn test-all
 []
 (refresh)
 (k/run :unit))
