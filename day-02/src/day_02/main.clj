(ns day-02.main
  (:require
   [clojure.string :as str]
   [clojure.math :as math]
   [clojure.java.io :as io])
  (:import
   (java.io StringReader BufferedReader)))

(defn str-reader
 [s]
 (BufferedReader. (StringReader. s)))

(defn split-words
 [s]
 (str/split s #"\s+"))

(defn parse-line
 [s]
 (mapv parse-long (split-words s)))

(defn safe?
 [xs]
 (let [deltas (mapv (fn [[x y]] (- y x)) (partitionv 2 1 xs))]
   (and (apply = (map math/signum deltas))
        (every? (fn [x] (< (abs x) 4)) deltas))))

(defn remove-index
 [index coll]
 (keep-indexed (fn [i elem] (when-not (= i index) elem)) coll))

(defn sublists
 [coll]
 (map (fn [i] (remove-index i coll)) (range (count coll))))

(defn almost-safe?
 [xs]
 (or (safe? xs)
     (some safe? (sublists xs))))

(defn part-01
 [rdr]
 (let [xform (comp (map parse-line)
                   (map (fn [line] (if (safe? line) 1 0))))]
   (transduce xform + (line-seq rdr))))

(defn part-02
 [rdr]
 (let [xform (comp (map parse-line)
                   (map (fn [line] (if (almost-safe? line) 1 0))))]
   (transduce xform + (line-seq rdr))))

(defn -main
 [& _args]
 (let [input-path "input/day_02_input.txt"]
   (with-open [rdr (io/reader input-path)]
     (printf "[day-02:part-01] %s%n" (part-01 rdr)))
   (with-open [rdr (io/reader input-path)]
     (printf "[day-02:part-02] %s%n" (part-02 rdr)))))
