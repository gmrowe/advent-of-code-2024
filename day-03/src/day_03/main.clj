(ns day-03.main
  (:require
   [clojure.java.io :as io]))

;; --- Day 3: Mull It Over ---
;; https://adventofcode.com/2024/day/3

(def mul-op-exp
  #"mul(?=\((\d+),(\d+)\))")

(def do-op
  #"do(?=\(\))")

(def dont-op
  #"don't(?=\(\))")

(defn evaluate-mul-op
 [[_ op-a op-b]]
 (* (parse-long op-a) (parse-long op-b)))

(defn part-01
 [rdr]
 (->> (slurp rdr)
      (re-seq mul-op-exp)
      (map evaluate-mul-op)
      (apply +)))


;; Returns a sequence of tokens. The tokens are one of the following
;; shapes:
;; ["mul" op1 op2]
;; ["do" nil nil]
;; ["dont" nil nil]
(defn parse-readable-data
 [s]
 (let [parser-pattern (re-pattern (str mul-op-exp "|" do-op "|" dont-op))]
   (re-seq parser-pattern s)))

(defn part-02
 [rdr]
 (second
  (reduce (fn [[state result] [tok-typ _ _ :as tok]]
            (cond
              (and (= tok-typ "mul") (= state :do))
              [state (+ result (evaluate-mul-op tok))]

              (= tok-typ "do") [:do result]
              (= tok-typ "don't") [:dont result]
              :else [state result]))
          [:do 0]
          (parse-readable-data (slurp rdr)))))

(defn -main
 [& _args]
 (let [input-path "input/day_03_input.txt"]
   (with-open [rdr (io/reader input-path)]
     (printf "[day-03:part-01] %s%n" (part-01 rdr)))
   (with-open [rdr (io/reader input-path)]
     (printf "[day-03:part-02] %s%n" (part-02 rdr)))))


