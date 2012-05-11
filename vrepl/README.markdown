C2 Visual REPL
==============

Render your Clojure visualizations as you write 'em.

To start from this git repo:

    lein run  --port 8987 --path resources/samples/

Serves up the final form of the most recently changed file in `resources/samples/` and automatically refreshes your browser.

If you don't have Leiningen installed, you can download the pre-built JAR file:

    wget http://keminglabs.com/c2/vrepl-1.1.1-standalone.jar
    java -jar vrepl-1.1.1-standalone.jar --extract

which will extract pre-built sample visualizations to the default `samples/` directory and start watching.
Change any files in there (or add a new ones) to update the web server visualization.



Todo
====

+ Auto-compile+refresh ClojureScript
+ Pretty stacktraces in browser
+ ProGuard or otherwise shrink UberJAR.
