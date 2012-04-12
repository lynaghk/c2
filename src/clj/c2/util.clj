(ns c2.util)

(defmacro typeof [x]
  `(goog.typeOf ~x))

(defmacro p [x]
  `(let [res# ~x]
     (.log js/console res#)
     res#))

(defmacro pp [x]
  `(let [res# ~x]
     (.log js/console (prn-str res#))
     res#))

(defmacro profile [descr & body]
  `(let [start# (.getTime (js/Date.) ())
         ret# (do ~@body)]
    (print (str ~descr ": " (- (.getTime (js/Date.) ()) start#) " msecs"))
    ret#))

(defmacro timeout [delay & body]
  `(js/setTimeout (fn [] ~@body) ~delay))
(defmacro interval [delay & body]
  `(js/setInterval (fn [] ~@body) ~delay))

(defmacro half [x] `(/ ~x 2))

(defn mapply
  "Useful for invoking functions with keyword args, thanks David Nolen."
  [f m]
  (apply f (apply concat m)))

(defn kwargify
  "Takes a function that expects a map and returns a function that accepts keyword arguments on its behalf, thanks Fogus."
  [f]
  (fn [& kwargs] (f (apply hash-map kwargs))))

(defmacro c2-obj
  "Macro that defines a record and corresponding constructor that accepts keyword arguments.
   The constructor function is defined to be the given name, with the record having an underscore prefix."
  [name fields-with-defaults & body]
  (let [recname (symbol (str "_" (clojure.core/name name)))]
    `(do
       (defrecord ~recname ~(into [] (map (comp symbol clojure.core/name)
                                          (keys fields-with-defaults)))
         ~@body)
       (defn ~name [& ~'kwargs]
         (~(symbol (str "map->" (clojure.core/name recname)))
          (merge ~fields-with-defaults (apply hash-map ~'kwargs)))))))


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
