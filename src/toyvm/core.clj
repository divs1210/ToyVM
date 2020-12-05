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
                  fn (nth stack nargs)
                  result (cond
                           (fn? fn)
                           (fn args)

                           (= ::fn (type fn))
                           (let [actuals (zipmap (:params fn)
                                                 args)
                                 body-env {:table actuals
                                           :parent (:env fn)}]
                             (->> (eval (:body-code fn)
                                        body-env)
                                  :stack
                                  first))
                           :else
                           (u/throw+ "Cannot call: " fn))]
              (recur (inc pc)
                     (cons result
                           (drop (inc nargs) stack))
                     env))

            :relative-jump-if-true
            (let [[condn stack] stack]
              (recur (inc (if condn
                            (+ pc arg)
                            pc))
                     stack
                     env))

            :relative-jump
            (recur (inc (+ pc arg))
                   stack
                   env)

            :make-function
            (let [nargs arg
                  [body-code params & stack] stack]
              (assert (= nargs (count params))
                      "Wrong number of args passed to fn.")
              (recur (inc pc)
                     (cons ^{:type ::fn}
                           {:params params
                            :body-code body-code
                            :env env}
                           stack)
                     env))

            ;; else
            (u/throw+ "Not implemented: " ins)))
        {:pc pc
         :stack stack
         :env env
         :code code}))))
