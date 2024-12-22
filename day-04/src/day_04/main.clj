(ns day-04.main
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]))

(defn rev-str
 [s]
 (str/join (reverse s)))

(defn invert-row-col
 [xss]
 (apply map vector xss))

;; The difference between row and column is constant
;; across forward diagonals. We use this to simplify
;; the access of forward diagonals.
(defn forward-diags
 [ss]
 (let [rows (count ss)
       cols (count (first ss))]
   (->> (for [row (range rows)
              col (range cols)]
          [(- row col) (nth (nth ss row) col)])
        (group-by first)
        vals
        (map #(map second %)))))

;; The sum of row and column is constant
;; across backward diagonals. We use this to simplify
;; the access of backward diagonals.
(defn backward-diags
 [ss]
 (let [rows (count ss)
       cols (count (first ss))]
   (->> (for [row (range rows)
              col (range cols)]
          [(+ row col) (nth (nth ss row) col)])
        (group-by first)
        vals
        (map #(map second %)))))

(defn search-string
 [data]
 (let [sep "|"
       lines (str/split-lines data)
       forward (str/join sep lines)
       forward-diags (str/join sep (map str/join (forward-diags lines)))
       backward-diags (str/join sep (map str/join (backward-diags lines)))
       vertical (str/join sep (map str/join (invert-row-col lines)))]
   (str/join sep
             [forward
              (rev-str forward)
              vertical
              (rev-str vertical)
              forward-diags
              (rev-str forward-diags)
              backward-diags
              (rev-str backward-diags)])))

(let [xss ["abc" "def" "GHI"]]
  (invert-row-col xss))

(defn part-01
 [rdr]
 (let [string-to-search (search-string (slurp rdr))]
   (count (re-seq #"(?:XMAS)" string-to-search))))

(defn iterate-xs
 [input]
 (let [height 3
       width 3
       rows (count input)
       cols (count (first input))]
   (for [row (range (- rows (- height 1)))
         col (range (- cols (- width 1)))
         :let [forward (map (fn [n] (nth (nth input (+ row n)) (+ col n))) (range width))
               backward (map (fn [n] (nth (nth input (+ row n)) (+ col (- 2 n))))
                             (range width))]]
     [forward backward])))

(defn part-02
 [rdr]
 (let [xs (iterate-xs (str/split-lines (slurp rdr)))]
   (->> xs
        (filter (fn [[fwd bwd]]
                  (or (and (= fwd (seq "MAS")) (= bwd (seq "MAS")))
                      (and (= fwd (seq "MAS")) (= bwd (seq "SAM")))
                      (and (= fwd (seq "SAM")) (= bwd (seq "SAM")))
                      (and (= fwd (seq "SAM")) (= bwd (seq "MAS"))))))
        count)))

(defn -main
 [& _args]
 (let [input-path "input/day_04_input.txt"]
   (with-open [rdr (io/reader input-path)]
     (printf "[day-04:part-01] %s%n" (part-01 rdr)))
   (with-open [rdr (io/reader input-path)]
     (printf "[day-04:part-02] %s%n" (part-02 rdr)))))

