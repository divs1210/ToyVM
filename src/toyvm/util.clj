(ns toyvm.util
  (:require [clojure.string :as str]))

(defn throw+ [& args]
  (throw (Exception. (str/join args))))

(defn read-file [filename]
  (read-string (slurp filename)))
