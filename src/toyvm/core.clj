(ns toyvm.core
  "Inspired by https://bernsteinbear.com/blog/bytecode-interpreters/"
  (:gen-class)
  (:require [clojure.pprint :refer [pprint]]
            [toyvm.bytecode-compiler :as bc]
            [toyvm.bytecode-interpreter :as bi]
            [toyvm.env :as env]))

(defn -main
  "Starts a REPL."
  []
  (println "==================")
  (println "=== ToyVM REPL ===")
  (println "==================")
  (println)
  (loop [env env/DEFAULT-ENV]
    (print "> ")
    (flush)
    (let [code-in (read)
          bytecode (bc/compile code-in)

          {:keys [stack env]}
          (try
            (bi/eval bytecode env)
            (catch Exception e
              (println (.getMessage e))
              {:env env}))]
      (pprint (first stack))
      (println)
      (recur env))))
