(ns day-05.main
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]))

(defn parse-int-lines-by-delim
 [s delim]
 (->> (str/split-lines s)
      (mapv (fn [line] (str/split line delim)))
      (mapv (fn [strs] (mapv parse-long strs)))))

(defn parse-input
 [input]
 (let [[rules pages] (str/split input #"\n\n")
       add-rule (fn [rules [before after]]
                  (-> rules
                      (update-in [before :after] (fnil conj #{}) after)
                      (update-in [after :before] (fnil conj #{}) before)))]
   {:rules (reduce add-rule {} (parse-int-lines-by-delim rules #"\|"))
    :pages (parse-int-lines-by-delim pages #",")}))

(defn tails
 [xs]
 (take-while seq (iterate next xs)))

(defn in-order?
 [rules page]
 (every?
  (fn [[n & afters]]
    (let [must-come-before? (get-in rules [n :before] #{})]
      (not-any? must-come-before? afters)))
  (tails page)))

(defn middle-element
 [xs]
 (nth xs (/ (count xs) 2)))

(defn part-01
 [rdr]
 (let [{:keys [rules pages]} (parse-input (slurp rdr))]
   (transduce (comp (filter (fn [page] (in-order? rules page)))
                    (map middle-element))
              +
              pages)))

(defn repair
 [rules pages]
 (if-let [n (first pages)]
   (let [more (rest pages)
         must-come-before? (get-in rules [n :before] #{})
         befores (filter must-come-before? more)
         afters (remove must-come-before? more)]
     (concat (repair rules befores) (cons n (repair rules afters))))
   pages))

(defn part-02
 [rdr]
 (let [{:keys [rules pages]} (parse-input (slurp rdr))]
   (transduce (comp (remove (fn [pages] (in-order? rules pages)))
                    (map (fn [pages] (repair rules pages)))
                    (map middle-element))
              +
              pages)))


(defn -main
 [& _args]
 (let [input-path "input/day_05_input.txt"]
   (with-open [rdr (io/reader input-path)]
     (printf "[day-05:part-01] %s%n" (part-01 rdr)))
   (with-open [rdr (io/reader input-path)]
     (printf "[day-05:part-02] %s%n" (part-02 rdr)))))
