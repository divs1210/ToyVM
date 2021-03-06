(ns toyvm.bytecode-interpreter
  (:gen-class)
  (:refer-clojure :exclude [eval])
  (:require [toyvm.env :as env]
            [toyvm.util :as u]
            [xodarap.core :refer [defrec rec]]))

(defrec eval
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
                   (env/define-in env arg (first stack)))

            :push-name
            (recur (inc pc)
                   (cons (env/lookup-in env arg)
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
                           (let [actuals (zipmap (:params fn) args)
                                 body-env {:table actuals
                                           :parent (:env fn)}
                                 result (rec (eval (:body-code fn)
                                                   body-env))]
                             (-> result :stack first))

                           (= ::recfn (type fn))
                           (let [param-bindings (zipmap (:params fn) args)
                                 this-binding {(:name fn) fn}
                                 actuals (merge param-bindings this-binding)
                                 body-env {:table actuals
                                           :parent (:env fn)}
                                 result (rec (eval (:body-code fn)
                                                   body-env))]
                             (-> result :stack first))

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

            :make-recursive-function
            (let [nargs arg
                  [body-code params name & stack] stack]
              (assert (= nargs (count params))
                      "Wrong number of args passed to fn.")
              (recur (inc pc)
                     (cons ^{:type ::recfn}
                           {:name name
                            :params params
                            :body-code body-code
                            :env env}
                           stack)
                     env))

            ;; else
            (u/throw+ "Not implemented: " ins)))
        {:stack stack
         :env env}))))

(defn -main
  "Interprets a bytecode edn file."
  [filename]
  (let [env env/DEFAULT-ENV
        code (u/read-file filename)]
    (eval code env)
    (println)))
