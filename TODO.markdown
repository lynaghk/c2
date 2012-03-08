Todo
====

Find a nice way to include useful data like US state geo data without blowing up the repo size.


Implement a Knuth & Plass line breaking system
  http://stackoverflow.com/questions/7046986/svg-using-getcomputedtextlength-to-wrap-text


Declarative behavior?
  http://jbeard4.github.com/SCION/


Is it possible to use pre/post conditions in development, but then strip them out when compiling to JS to save space?


Implement scales, &c. as "objects":

    (def s (scale :domain [0 50] :range [0 1]))
    (s 25) ;=> 0.5
    (:domain s) ;=> [0 50]


Visual REPL
-----------

Alert user on LiveReloadDisconnect
Pretty stack traces



Design decisions
================

Should thing like svg.arc be higher order functions (as in D3), or just work as basic utilities?
The latter is simpler, and I'm not convinced of the advantages of defining things like arc helpers away from where they'll be used.
