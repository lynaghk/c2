(ns c2.util)

(defmacro typeof [x]
  `(goog.typeOf ~x))

(defmacro p [x]
  `(do (.log js/console ~x)
       ~x))

(defmacro pp [x]
  `(do (.log js/console (pr-str ~x))
       ~x))

(defmacro timeout [delay f] `(js/setTimeout ~f ~delay))
(defmacro interval [delay f] `(js/setInterval ~f ~delay))
(defmacro half [x] `(/ ~x 2))

(defn mapply
  "Useful for invoking functions with keyword args"
  [f m]
  (apply f (apply concat m)))


(defmacro c2-obj
  "Macro that expands into reify with getters/setters for fields and Associative implementation that calls constructor with keyword args of fields."
  [constructor iface fields & body]
  `(reify
     ~iface
     ~@(concat (apply concat (for [f fields]
                               (list `(~f [~'_] ~f)
                                     `(~f [~'_ ~'x] (mapply ~constructor (assoc ~'args ~f ~'x))))))
         `[clojure.lang.ILookup (~'valAt [~'_ ~'k] (~'args ~'k))
           clojure.lang.Associative (~'assoc [~'_ ~'k ~'v] (mapply ~constructor (assoc ~'args ~'k ~'v)))]
         body)))

(defn dont-carity
  "Execute fn with args, catching wrong-arity errors and retrying with (butlast args).
Currently, there is no arity-checking on ClojureScript anon functions, so this is a serverside prevention measure only."
  ;;Lets turn this into a macro handling the general case sometime, eh?
  ([f] (f))
  ([f a] (try (f a)
              (catch clojure.lang.ArityException e (dont-carity f))))
  ([f a b] (try (f a b)
                (catch clojure.lang.ArityException e (dont-carity f a)))))

(defn f-c
  "Call f with args if it implement IFn, otherwise just return f. Useful to let people pass in fns or constants to some helpers."
  [f & args]
  (if (ifn? f)
    (apply f args)
    f))
