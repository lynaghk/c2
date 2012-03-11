
      _____   ___  
     / ____| |__ \ 
    | |         ) |
    | |        / / 
    | |____   / /_ 
     \_____| |____|

    Declarative visualization in Clojure(Script)




EVERYTHING IS ALPHA
===================

ClojureScript is new.
This library is newer.
We have no idea what's going on, and we're going to break a lot of stuff figuring it out.
Stable 1.0 release planned for April 2012.



Play around
===========

See `vrepl/README.markdown` for instructions on using the built-in examples+interactive-development server.


Development
===========

If you want to work on c2 itself or just want a sandbox to play in, this repository has everything you need.
First run

    git submodule update --init
    cake deps

to get dependencies, then bootstrap-install Clojurescript:

    cd vendor/clojurescript
    ./script/bootstrap

and add its jars to your classpath.
This repository includes a `.cake/config` with the appropriate paths.



Testing
=======

Most of C2 is written in platform-agnostic Clojure and tested with Midje.
Run

    lein midje --autotest

to start a test watcher, which will automatically reload namespaces and run tests when source or test files are changed.   

For ClojureScript-specific integration testing, use our highly advanced, PhantomJS-powered "list-of-assertions" testing framework:

    cake run script/compile_tests.clj && phantomjs test/integration/runner.coffee

or, if you're too cool to go headless, open up `test/integration/runner.html` in your browser.
