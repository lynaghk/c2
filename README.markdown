
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


Experimentation / Development
=============================

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

Use PhantomJS with highly advanced "list-of-assertions" testing framework:

    cake run script/compile_tests.clj && phantomjs test/integration/runner.js

or, if you're too cool to go headless, open up `test/integration/runner.html` in your browser.
