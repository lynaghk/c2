;;Core functions that map data to DOM elements.
(ns c2.core
  (:use-macros [c2.util :only [p pp]])
  (:require [singult.core :as singult]

            ;;These namespaces required here so they're sucked into deps calculation.
            ;;(Some macros expand with calls to fns in these namespaces)
            [c2.dom :as dom]
            [reflex.core :as reflex]))

(def node-data singult/node-data)
(def unify singult/unify)
