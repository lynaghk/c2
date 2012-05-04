
      _____   ___  
     / ____| |__ \ 
    | |         ) |
    | |        / / 
    | |____   / /_ 
     \_____| |____|

    Declarative visualization in Clojure(Script)


C2 is a [D3](http://mbostock.github.com/d3)-inspired data visualization library for Clojure and ClojureScript.
Map data directly to DOM-elements on the client-side or Hiccup vectors on the server-side and leverage the existing power of HTML, SVG, CSS, and the web ecosystem to construct bespoke data visualizations.
For examples, see the [official site](http://keminglabs.com/c2/).


EVERYTHING IS ALPHA
===================
ClojureScript is new.
This library is newer.
We have no idea what's going on, and we're going to break a lot of stuff figuring it out.



Play around
===========
See [vrepl/README.markdown](https://github.com/lynaghk/c2/blob/master/vrepl/README.markdown) for instructions on using the built-in examples+interactive-development server (Clojure-only).

There's also a two minute [screencast](https://www.youtube.com/watch?v=Urg79FmQnYs), and a longer [overview/tutorial](http://www.youtube.com/watch?feature=player_detailpage&v=T83P3PVSy_8#t=510s) video on the library.

To use from Clojure/ClojureScript add this to your `project.clj`:

    [com.keminglabs/c2 "0.1.0-beta2"]

Because the complex classpath setup, Leiningen 2.0.0 is required.
For ClojureScript development, I highly recommend using [lein-cljsbuild](https://github.com/emezeske/lein-cljsbuild).


Differences from D3
===================

Language
--------
D3 is written in JavaScript and C2 is written in Clojure.
Clojure is a richer language than JavaScript, with features like destructuring, lazy evaluation, namespaces, and macros that JavaScript does not provide.
Since Clojure runs on the Java Virtual Machine, it's possible to work with much larger data sets than JavaScript can handle, directly access datastores, and perform advanced computations (in parallel).

C2 leverages ClojureScript, a Clojure-to-JavaScript compiler, which means you can take advantage of the expressiveness of Clojure while maintaining the reach of JavaScript.
The ClojureScript compiler is built on Google's Closure compiler, and in many cases your visualizations may compile to JavaScript with a smaller file size than the D3.js library itself!

Hierarchy
---------
C2 uses Hiccup vectors to declaratively specify DOM structure, and it's possible to map each datum to an arbitrary hierarchy of nodes.
For instance, you could build a bar chart's bars and labels at the same time:

```clojure
(unify! "#barchart"
        {"A" 1, "B" 2, "C" 4, "D" 3}
        (fn [[label val]]
          [:div.bar
           [:div.bar-fill {:style {:width (x-scale val)}}]
           [:span.label label]]))
```

whereas in D3 (because of the chained syntax) you'd have to build the bars and labels in separate passes (or manually, using `each`).

No Animation
------------
Unlike D3, C2 does not have an animation system, although you can use CSS transitions to perform animations.

Consistent mapping
------------------
There is no enter/exit/update distinction; the data-to-DOM mapping is always enforced.
If you want to circumvent it and manipulate nodes manually, you can provide custom functions for enter, exit, and update.



Roadmap
=======

+ Moar documentation, consistent docstring format.

+ Official release


Testing
=======

Most of C2 is written in platform-agnostic Clojure and tested with Midje.
Run

    lein midje --autotest

to start a test watcher, which will automatically reload namespaces and run tests when source or test files are changed.   

For ClojureScript-specific integration testing, you can run our highly advanced, PhantomJS-powered "list-of-assertions" testing framework:

    lein cljsbuild test

or, if you're too cool to go headless:

    lein cljsbuild once

then open up `test/integration/runner.html` in your browser.
