;;Core functions that map data to DOM elements.
(ns c2.core
  (:use-macros [c2.util :only [p pp]])
  (:require [singult.core :as singult]

            ;;These namespaces required here so they're sucked into deps calculation.
            ;;(Some macros expand with calls to fns in these namespaces)
            [c2.dom :as dom]
            [reflex.core :as reflex]))

(def node-data singult/node-data)

(defn unify [data mapping & args]
  ;;Execute the mapping on the first datum so that atoms within the mapping fn will be derefed.
  ;;This should eliminate confusion wherin c2.util#bind! doesn't pick up on dependencies within the unify mapping fn (because the mapping fn isn't executed until rendering, which happens after computed-observable dependencies are calculated).
  (when (seq data)
    (mapping (first data)))
  (apply singult/unify data mapping args))
