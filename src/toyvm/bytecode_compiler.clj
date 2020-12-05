(ns toyvm.bytecode-compiler
  (:gen-class)
  (:refer-clojure :exclude [compile])
  (:require [clojure.pprint :refer [pprint]]
            [toyvm.util :as u]))

(defn compile
  "Recursively compiles the given form to bytcodes."
  [exp]
  (cond
    (integer? exp)
    [[:push-const exp]]

    (symbol? exp)
    [[:push-name exp]]

    (seq exp)
    (case (first exp)
      ;; special forms go here
      def
      (let [[_ name subexp] exp]
        (concat (compile subexp)
                [[:store-name name]]))

      if
      (let [[_ condn then else] exp
            then-code (compile then)
            else-code (concat (compile else)
                              [[:relative-jump (count then-code)]])]
        (concat (compile condn)
                [[:relative-jump-if-true (count else-code)]]
                else-code
                then-code))

      fn
      (let [[_ params body] exp]
        [[:push-const params]
         [:push-const (compile body)]
         [:make-function (count params)]])

      ;; else fn call
      (let [[fname & args] exp
            nargs (count args)
            arg-code (mapcat compile args)]
        (concat (compile fname)
                arg-code
                [[:call-function nargs]])))

    :else
    (u/throw+ "Not implemented: " exp)))

(defn -main
  "Reads the lisp forms in the given edn file
  compiles all of them to bytecode, and
  writes to out.edn"
  [filename]
  (let [bytecode (->> filename
                      u/read-file 
                      (mapcat compile))
        out-text (with-out-str
                   (pprint bytecode))]
    (spit "out.edn" out-text)))
