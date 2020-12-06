(ns toyvm.env
  (:require [toyvm.util :as u]))

(def ^:const DEFAULT-ENV
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
         (apply > args))
    'print (fn [args]
             (apply print args))}})

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

(defn collapse
  "Collapses all entries
  in the environment heirarchy
  into a single hashmap."
  [env]
  (reduce (fn [entries env]
            (merge (:table env)
                   entries))
          {}
          (->> env
               (iterate :parent)
               (take-while some?))))
