def compile() 
  Kernel::system "cake run script/compile_tests.clj && phantomjs test/integration/runner.coffee"
end

watch( '^test/(.*\.cljs)' )  { |m| compile() }
watch( '^src/cljs/(.*\.cljs)' )  { |m| compile() }
