
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

See [vrepl/README.markdown](https://github.com/lynaghk/c2/blob/master/vrepl/README.markdown) for instructions on using the built-in examples+interactive-development server.

There's also a two minute [screencast](https://www.youtube.com/watch?v=Urg79FmQnYs).

The VREPL doesn't contain any ClojureScript---nice ClojureScript support+examples coming as soon as Clojure/ClojureScript code-sharing issues are resolved in Lein tooling.

To use in Clojure/ClojureScript, add this to your `project.clj`:

    [com.keminglabs/c2 "0.0.1-SNAPSHOT"]

Testing
=======

Most of C2 is written in platform-agnostic Clojure and tested with Midje.
Run

    lein midje --autotest

to start a test watcher, which will automatically reload namespaces and run tests when source or test files are changed.   

For ClojureScript-specific integration testing, use our highly advanced, PhantomJS-powered "list-of-assertions" testing framework:

    cake run script/compile_tests.clj && phantomjs test/integration/runner.coffee

or, if you're too cool to go headless, open up `test/integration/runner.html` in your browser.
