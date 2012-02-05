watch( '^test/(.*\.cljs)' )  { |m| Kernel::system "cake run script/compile_tests.clj && phantomjs test/integration/runner.coffee" }
