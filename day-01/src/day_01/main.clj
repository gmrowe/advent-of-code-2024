(ns day-01.main
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]))

(defn split-whitespace
 [s]
 (str/split s #"\s+"))

(defn unpair
 [pairs]
 (reduce (fn [[as bs] [a b]] [(conj as a) (conj bs b)]) [[] []] pairs))

(defn parse-input
 [input-rdr]
 (->> (line-seq input-rdr)
      (mapv split-whitespace)
      unpair
      (mapv (fn [ss] (mapv parse-long ss)))))

(defn part-01
 [[xs ys]]
 (->> [(sort xs) (sort ys)]
      (apply map (fn [x y] (abs (- x y))))
      (apply +)))

(defn part-02
 [[xs ys]]
 (let [freqs (frequencies ys)]
   (reduce (fn [score n] (+ score (* n (get freqs n 0)))) 0 xs)))

(defn -main
 [& _args]
 (let [input-path "input/day_01_input.txt"
       input (with-open [rdr (io/reader input-path)] (parse-input rdr))]
   (printf "[day-01:part-01] %s%n" (part-01 input))
   (printf "[day-02:part-02] %s%n" (part-02 input))))
