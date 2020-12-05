(ns toyvm.core
  "Clojure translation of: https://bernsteinbear.com/blog/bytecode-interpreters/"
  (:gen-class)
  (:refer-clojure :exclude [compile eval])
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
      'def
      (let [[_ name subexp] exp]
        (concat (compile subexp)
                [[:store-name name]]))

      ;; else fn call
      (let [[fname & args] exp
            nargs (count args)
            arg-code (mapcat compile args)]
        (concat (compile fname)
                arg-code
                [[:call-function nargs]])))

    :else
    (u/throw+ "Not implemented: " exp)))

(defn define-in
  [env key val]
  (assoc-in env [:table key] val))

(defn lookup-in
  [env key]
  (cond
    (contains? (:table env) key)
    (get-in env [:table key])

    (contains? env :parent)
    (recur (:parent env) key)

    :else
    (u/throw+ "Not defined: " key)))

(defn eval
  [code env]
  (let [code (vec code)
        len (count code)]
    (loop [pc 0
           stack []
           env env]
      (if (< pc len)
        (let [ins (code pc)
              [op arg] ins]
          (case op
            :push-const
            (recur (inc pc)
                   (cons arg stack)
                   env)

            :store-name
            (recur (inc pc)
                   (rest stack)
                   (define-in env arg (first stack)))

            :push-name
            (recur (inc pc)
                   (cons (lookup-in env arg)
                         stack)
                   env)

            :call-function
            (let [nargs arg
                  args (reverse (take nargs stack))
                  fn (nth stack nargs)]
              (recur (inc pc)
                     (cons (fn args)
                           (drop (inc nargs) stack))
                     env))

            ;; else
            (u/throw+ "Not implemented: " ins)))
        {:pc pc
         :stack stack
         :env env}))))
