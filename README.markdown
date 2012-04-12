
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
Stable 1.0 release planned for May 2012.

Play around
===========

See [vrepl/README.markdown](https://github.com/lynaghk/c2/blob/master/vrepl/README.markdown) for instructions on using the built-in examples+interactive-development server (Clojure-only).

There's also a two minute [screencast](https://www.youtube.com/watch?v=Urg79FmQnYs).

To use from Clojure/ClojureScript add this to your `project.clj`:

    [com.keminglabs/c2 "0.1.0-beta2-SNAPSHOT"]

For ClojureScript development, I highly recommend using [lein-cljsbuild](https://github.com/emezeske/lein-cljsbuild).

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
