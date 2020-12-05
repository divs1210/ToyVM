(ns toyvm.bytecode-compiler
  (:refer-clojure :exclude [compile])
  (:require [toyvm.util :as u]))

(defn compile
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
