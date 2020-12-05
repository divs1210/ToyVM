(defproject toyvm "0.1.0-SNAPSHOT"
  :description "ToyVM: Bytecode VM for a simple lisp"
  :url "http://example.com/divs1210/toyvm"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :main ^:skip-aot toyvm.core
  :target-path "target/%s"
  :aliases {"bcompile" ["run" "-m" "toyvm.bytecode-compiler"]}
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
