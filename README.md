# ToyVM

**Bytecode VM for a simple lisp**

* At the REPL, all forms are converted to bytecode, which is interpreted
* Possible to AOT compile a lisp file to bytecode
* Possible to directly interpret a bytecode file

Heavily inspired by [this blogpost](https://bernsteinbear.com/blog/bytecode-interpreters/)
by **Max Bernstein** with the following changes:
* late binding to enable recursion
* no `define`, as recursion possible via normal lambdas
* `lambda` is called `fn`
* written in Clojure in a functional manner

## Usage

### Start a REPL

    $ rlwrap lein do clean, run
    ==================
    === ToyVM REPL ===
    ==================

    > (def dec
        (fn (x)
          (- x 1)))
    nil

    > (dec 43)
    42

### Compile to bytecode

Compile the example file:

    $ cat factorial.edn
    [

     (def dec
       (fn (n)
         (- n 1)))

     (def fact
       (fn (n)
         (if (< n 2)
           1
           (* n (fact (dec n))))))

     (print (fact 5))

    ]

    $ lein bcompile factorial.edn

    $ cat out.edn
    [[:push-const [n]]
     [:push-const
      [[:push-name -]
       [:push-name n]
       [:push-const 1]
       [:call-function 2]]]
     [:make-function 1]
     [:store-name dec]
     [:push-const [n]]
     [:push-const
      [[:push-name <]
       [:push-name n]
       [:push-const 2]
       [:call-function 2]
       [:relative-jump-if-true 9]
       [:push-name *]
       [:push-name n]
       [:push-name fact]
       [:push-name dec]
       [:push-name n]
       [:call-function 1]
       [:call-function 1]
       [:call-function 2]
       [:relative-jump 1]
       [:push-const 1]]]
     [:make-function 1]
     [:store-name fact]
     [:push-name fact]
     [:push-const 5]
     [:call-function 1]]

### Run bytecode file

Interpret the compiled output:

    $ lein binterpret out.edn
    120

### Compile and run in one go

    $ lein do clean, bcompile factorial.edn, binterpret out.edn
    120

## License

Copyright © 2020 Divyansh Prakash

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
