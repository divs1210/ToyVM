(ns toyvm.core
  "Inspired by https://bernsteinbear.com/blog/bytecode-interpreters/"
  (:gen-class)
  (:require [toyvm.repl :refer [repl]]))

(defn -main
  "Starts a command line REPL."
  []
  (println "==================")
  (println "=== ToyVM REPL ===")
  (println "==================")
  (println)
  (repl))
