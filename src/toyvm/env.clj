(ns toyvm.env
  (:require [toyvm.util :as u]))

(def DEFAULT-ENV
  {:table
   {'+ (fn [args]
         (apply +' args))
    '- (fn [args]
         (apply - args))
    '* (fn [args]
         (apply *' args))
    '/ (fn [args]
         (apply / args))
    '< (fn [args]
         (apply < args))
    '> (fn [args]
         (apply > args))}})

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
