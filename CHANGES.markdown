Change Log
==========

0.2.2
-----
+ Fix svg/axis tick unification for changing scales
+ Add matchesSelector shim for browser compatability
+ Add histogram layout helper (thanks @jblomo!)

0.2.1
-----
This is a bugfix + upstream lib release, see [Singult](https://github.com/lynaghk/singult) repo for more details.

+ Speedup SVG axis rendering performance.
+ Nicer tick search clamping behavior.
+ Added quantile statistic.


0.2.0
-----
+ Add `bind!` macro, the new recommended way to bind data to the DOM.
+ Remove overly-complex, side-effecting `unify!` fn in favor of Singult implementation.
+ Remove Hiccup compiler in favor of Singult implementation.
+ Make Closure DOM manipulation wrappers more consistent; implement faster IDom protocol instead of using multimethods.

0.1.2
-----
+ Use case instead of core.match in axis calculation (due to AOT-compile bug on CLJ).

0.1.1
-----
+ Added median, inverse trig, and haversine fns.
+ Added `IInvertable` protocol for scales (only linear implemented).
+ dom/attr will remove attributes with `nil` values.
+ `unify!` handles `(mapping d)` => `nil` properly.


0.1.0
-----
+ Initial release.
