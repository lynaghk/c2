(ns c2.cassowary
  (:refer-clojure :exclude [+ - =])
  ;;Need to require this for Closure Advanced Mode?
  ;;Buy VanderHart a beer...
  #_(:require [Cl.Variable :as clv]
              [Cl.SimplexSolver :as solver]
              [Cl.LinearEquation :as cle]))

(defn cvar
  ([] (cvar 0))
  ([val] (js/Cl.Variable. (js/parseFloat val))))

(defn simplex-solver [] (js/Cl.SimplexSolver.))

(defn constrain! [solver constraint] (.addConstraint solver constraint))
(defn stay! [solver cvar] (.addStay solver cvar))

(defn contains-cassowary? [& args]
  (if (some #(or (instance? js/Cl.Variable %)
                 (instance? js/Cl.LinearExpression %)) args)
    :cassowary-var
    :number))

(defmulti + contains-cassowary?)
(defmulti - contains-cassowary?)
(defmulti = contains-cassowary?)

(defmethod + :number [& args] (apply clojure.core/+ args))
(defmethod = :number [& args] (apply clojure.core/= args))
(defmethod - :number [& args] (apply clojure.core/- args))

(defmethod + :cassowary-var [& args] (apply js/Cl.CL.Plus args))
(defmethod = :cassowary-var [a b] (js/Cl.LinearEquation. a b))
(defmethod - :cassowary-var [& args] (apply js/Cl.CL.Minus args))
