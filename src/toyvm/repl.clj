(ns toyvm.repl
  (:require [clojure.pprint :refer [pprint]]
            [toyvm.bytecode-compiler :as bc]
            [toyvm.bytecode-interpreter :as bi]
            [toyvm.env :as env]))

(defn repl [& {:keys [instream outstream]
               :or {instream *in*
                    outstream *out*}}]
  (binding [*in* instream
            *out* outstream]
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
        (recur env)))))
