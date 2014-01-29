      _____   ___  
     / ____| |__ \ 
    | |         ) |
    | |        / / 
    | |____   / /_ 
     \_____| |____|

    Declarative visualization in Clojure(Script)


C2 is a [D3](http://mbostock.github.com/d3)-inspired data visualization library for Clojure and ClojureScript.
As with D3, the core idea is to build declarative mappings from your data to HTML or SVG markup.
This lets you leverage CSS and the existing web ecosystem to construct bespoke data visualizations.

C2 encourages a "data-driven" approach to application design.
Compose pure functions to map your data to native Clojure vectors and maps that represent the DOM, and then let the library handle rendering into actual elements (on the clientside) or a string of markup (on the serverside).

In a browser, C2 handles DOM updates as well, so it will add/remove nodes and set attributes/styles when your data changes so you don't have to deal with the incidental state and complexity of low-level, imperative manipulation.
To see what this "data-driven" approach looks like in a simple application, see this [C2-powered todo list](https://github.com/lynaghk/c2-demos/tree/master/todoMVC).

See also:

[Google group](https://groups.google.com/forum/?hl=en&fromgroups#!forum/c2-cljs)

[Example visualizations](http://keminglabs.com/c2/)

[Annotated source](http://keminglabs.com/c2/docs/)


EVERYTHING IS ALPHA
===================
ClojureScript is new.
This library is newer.
We have no idea what's going on, and we're going to break a lot of stuff figuring it out.


Play around
===========
See [vrepl/README.markdown](https://github.com/lynaghk/c2/blob/master/vrepl/README.markdown) for instructions on using the built-in examples+interactive-development server (Clojure-only).
For a full ClojureScript application example, check out the [C2-powered todo list](https://github.com/lynaghk/c2-demos/tree/master/todoMVC).

There's also a two minute [screencast](https://www.youtube.com/watch?v=Urg79FmQnYs), and a longer [overview/tutorial](http://www.youtube.com/watch?feature=player_detailpage&v=T83P3PVSy_8#t=510s) video on the library.

To use from Clojure/ClojureScript add this to your `project.clj`:

    [com.keminglabs/c2 "0.2.3"]

Leiningen 2.0.0 is required.
For ClojureScript development, check out [lein-cljsbuild](https://github.com/emezeske/lein-cljsbuild).


Differences from D3
===================

Language
--------
D3 is written in JavaScript and C2 is written in Clojure.
Clojure is a richer language than JavaScript, with features like destructuring, lazy evaluation, namespaces, and macros, which JavaScript does not provide.
Since Clojure runs on the Java Virtual Machine, it's possible to work with much larger data sets than JavaScript can handle, directly access datastores, and perform advanced computations (in parallel).

C2 also leverages ClojureScript, a Clojure-to-JavaScript compiler, so you can take advantage of the expressiveness of Clojure while maintaining the reach of JavaScript.
The ClojureScript compiler is built on Google's Closure compiler, and in many cases your visualizations may compile to JavaScript with a smaller file size than the D3.js library itself!

(If you want a ClojureScript library that actually leverages D3, take a look at the awesomely polyfilled [Strokes](https://github.com/dribnet/strokes) library.)

View is data
-------------
Rather than think of DOM nodes as foreign objects to be manipulated with methods like `addChild` or `setClass`, in C2 you build the DOM you want using standard Clojure data structures like vectors and maps.
That is, you just specify what you want on the DOM by composing pure functions.
Since all you're doing is data transformation, you don't actually need a DOM; that means you can

+ test your code without a browser
+ render all of your visualizations on the server and send down pure markup
+ render visualizations with computationally-heavy mappings (e.g., detailed map projections or Hilbert curves) on background web workers

With standard data structures, you're also not limited (as in D3) to mapping each datum to a single DOM node.
For instance, you could build a bar chart's title, bars, and their labels all at the same time:

```clojure
(bind! "#barchart"
       [:div#barchart
        [:h2 "Rad barchart!"]
        [:div.bars
         (unify {"A" 1, "B" 2, "C" 4, "D" 3}
                (fn [[label val]]
                  [:div.bar
                   [:div.bar-fill {:style {:width (x-scale val)}}]
                   [:span.label label]]))]])
```

whereas in D3 (because of the chained syntax) you'd have to build the bars and labels in separate passes (or manually, using `each`).

Sensible state
--------------
All of Clojure's data structures are immutable; if you want to model mutable state, explicit semantics are required.
Both Clojure and ClojureScript have the *atom* reference type, which just "points" to an immutable value.
If you want to "change" an atom, you point it to another immutable value.
To get at the value, you have to explicitly dereference it using the `@` syntax (e.g., `@a ;;=> "stuff pointed at by a"`)

C2 takes advantage of this to automatically setup *bindings*.
In the previous example, if the bar chart's data were dereferenced from an atom rather than being inlined (`{"A" 1, "B" 2, "C" 4, "D" 3}`), then the `bind!` macro would automatically watch that atom and re-render the view when the atom pointed to a new value.
(Watchers are added to every atom that is dereferenced within `bind!`.)

No animation
------------
Unlike D3, C2 does not have an animation system, although you can use CSS transitions to perform animations.


Development
===========
The C2 library itself is a grab bag of data visualization helpers (scales, map projections, &c.) and some wrappers around the Google Closure library's DOM-manipulation and event handling facilities.
The core DOM-manipulation and binding functionality is provided by two other libraries:

+ [Singult](https://github.com/lynaghk/singult) renders hiccup vectors into DOM-elements and merges into existing DOM trees.
  Singult is written in CoffeeScript (for speed) and does not depend on ClojureScript at all, so you can use it from plain JavaScript if you like.
+ [Reflex](https://github.com/lynaghk/reflex) provides a macro that "captures" dereferenced atoms, which is what powers C2's `bind!` macro.
  Reflex also provides some macros to help coordinate (e.g., automatically add/remove watchers on multiple atoms).


Most of C2 is written in platform-agnostic Clojure using [cljx](http://github.com/lynaghk/cljx), a Clojure/ClojureScript code generator.
If you edit a `.cljx` file, run

    lein cljx

to regenerate the corresponding `.clj` and `.cljs` files.

Unit tests are written in [Midje](https://github.com/marick/Midje); run with:

    lein midje

For ClojureScript-specific integration testing, you can run the highly advanced, PhantomJS-powered "list-of-assertions" testing framework:

    lein cljsbuild test

or, if you're too cool to go headless:

    lein cljsbuild once
